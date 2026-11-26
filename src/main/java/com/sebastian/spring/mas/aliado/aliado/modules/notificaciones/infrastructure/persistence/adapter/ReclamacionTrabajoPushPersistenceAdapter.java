package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.persistence.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.DestinoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.LoteReclamadoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.MotivoCierreTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.ReclamacionTrabajoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.SenalTrabajoPush;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Adaptador PostgreSQL de reclamación del worker push (V9, F09-PUSH-07).
 *
 * <p>Selecciona padres y entregas elegibles con {@code FOR UPDATE SKIP LOCKED}, de modo
 * que dos workers no comparten una reclamación vigente. Cada fila recibe su propio
 * identificador y un vencimiento calculado con el reloj de PostgreSQL. La selección y la
 * actualización corren dentro de la transacción que abre
 * {@code ReclamacionTrabajoPushAdapter}; {@code pg_notify} se emite en esa misma
 * transacción y PostgreSQL solo lo entrega al confirmar.</p>
 *
 * <p>Las escrituras de resultado actualizan condicionadas al identificador de reclamación
 * vigente y a su plazo, así que un resultado tardío afecta cero filas. La reclamación
 * vencida vuelve a ser elegible sin depender de una publicación nueva.</p>
 */
@Component
public class ReclamacionTrabajoPushPersistenceAdapter implements ReclamacionTrabajoPushRepository {

    private static final String SQL_PREPARACIONES = """
            SELECT id
            FROM trabajos_push
            WHERE estado = 'PENDIENTE_PREPARACION'
              AND (reclamacion_vence_en IS NULL OR reclamacion_vence_en <= CURRENT_TIMESTAMP)
            ORDER BY encolado_en, id
            LIMIT ?
            FOR UPDATE SKIP LOCKED
            """;

    private static final String SQL_RECLAMAR_PREPARACION = """
            UPDATE trabajos_push
            SET reclamacion_id = ?,
                reclamacion_vence_en = CURRENT_TIMESTAMP + make_interval(secs => ?)
            WHERE id = ?
              AND estado = 'PENDIENTE_PREPARACION'
              AND (reclamacion_vence_en IS NULL OR reclamacion_vence_en <= CURRENT_TIMESTAMP)
            RETURNING reclamacion_vence_en
            """;

    private static final String SQL_ENTREGAS = """
            SELECT e.id, e.trabajo_id, e.instalacion_id, e.generacion
            FROM entregas_push e
            JOIN trabajos_push t ON t.id = e.trabajo_id
            WHERE e.estado = 'PENDIENTE'
              AND (e.proximo_intento_en <= CURRENT_TIMESTAMP OR t.expira_en <= CURRENT_TIMESTAMP)
              AND (e.reclamacion_vence_en IS NULL OR e.reclamacion_vence_en <= CURRENT_TIMESTAMP)
              AND t.estado = 'PREPARADO'
            ORDER BY e.proximo_intento_en, e.id
            LIMIT ?
            FOR UPDATE OF e SKIP LOCKED
            """;

    private static final String SQL_RECLAMAR_ENTREGA = """
            UPDATE entregas_push
            SET reclamacion_id = ?,
                reclamacion_vence_en = CURRENT_TIMESTAMP + make_interval(secs => ?)
            WHERE id = ?
              AND estado = 'PENDIENTE'
              AND (reclamacion_vence_en IS NULL OR reclamacion_vence_en <= CURRENT_TIMESTAMP)
            RETURNING reclamacion_vence_en
            """;

    private static final String SQL_CONFIRMAR_PREPARACION = """
            UPDATE trabajos_push
            SET estado = 'PREPARADO',
                reclamacion_id = NULL,
                reclamacion_vence_en = NULL
            WHERE id = ?
              AND estado = 'PENDIENTE_PREPARACION'
              AND reclamacion_id = ?
              AND reclamacion_vence_en > CURRENT_TIMESTAMP
            """;

    private static final String SQL_RESULTADO_ENTREGA = """
            UPDATE entregas_push e
            SET estado = ?,
                intentos = ?,
                proximo_intento_en = ?,
                id_mensaje_fcm = ?,
                ultimo_codigo_error = ?,
                finalizada_en = CASE WHEN ? THEN NULL ELSE CURRENT_TIMESTAMP END,
                reclamacion_id = NULL,
                reclamacion_vence_en = NULL
            WHERE e.id = ?
              AND e.estado = 'PENDIENTE'
              AND e.reclamacion_id = ?
              AND e.reclamacion_vence_en > CURRENT_TIMESTAMP
              AND e.generacion = ?
              AND EXISTS (SELECT 1 FROM trabajos_push t
                          WHERE t.id = e.trabajo_id AND t.estado <> 'FINALIZADO')
            """;

    /**
     * Libera la reclamación de una entrega sin tocar su estado ni su próximo intento: la
     * fila vuelve a ser reclamable con un vencimiento nuevo. Una entrega terminal o una
     * reclamación reemplazada no cambian.
     */
    private static final String SQL_LIBERAR_RECLAMACION_ENTREGA = """
            UPDATE entregas_push
            SET reclamacion_id = NULL,
                reclamacion_vence_en = NULL
            WHERE id = ?
              AND reclamacion_id = ?
              AND estado = 'PENDIENTE'
            """;

    /**
     * Plazos que pueden cambiar la elegibilidad. De una entrega pendiente solo cuenta si su
     * padre sigue {@code PREPARADO}: las filas huérfanas de un padre cerrado no deben
     * programar un temporizador de retraso cero. El plazo de una entrega reclamada es su
     * vencimiento (GREATEST ignora el intento ya pasado), no un despertar inmediato que
     * repetiría la consulta mientras otra instancia mantiene la reclamación.
     */
    private static final String SQL_PROXIMO_PLAZO = """
            SELECT MIN(plazo) AS plazo
            FROM (
                SELECT reclamacion_vence_en AS plazo FROM trabajos_push
                WHERE reclamacion_vence_en IS NOT NULL
                UNION ALL
                SELECT GREATEST(e.proximo_intento_en, e.reclamacion_vence_en)
                FROM entregas_push e
                JOIN trabajos_push t ON t.id = e.trabajo_id
                WHERE e.estado = 'PENDIENTE'
                  AND t.estado = 'PREPARADO'
                UNION ALL
                SELECT expira_en FROM trabajos_push
                WHERE estado <> 'FINALIZADO'
            ) plazos
            """;

    private static final String SQL_TRABAJO = """
            SELECT id, notificacion_id, estado, encolado_en, expira_en, motivo_final
            FROM trabajos_push WHERE id = ?
            """;

    private static final String SQL_ENTREGA = """
            SELECT id, trabajo_id, instalacion_id, generacion, estado, intentos, proximo_intento_en
            FROM entregas_push WHERE id = ?
            """;

    private static final String SQL_TRABAJO_EXPIRADO = """
            SELECT expira_en <= CURRENT_TIMESTAMP FROM trabajos_push WHERE id = ?
            """;

    private static final String SQL_CREAR_ENTREGA = """
            INSERT INTO entregas_push (id, trabajo_id, instalacion_id, generacion, estado,
                                       intentos, proximo_intento_en)
            VALUES (?, ?, ?, ?, 'PENDIENTE', 0, CURRENT_TIMESTAMP)
            ON CONFLICT (trabajo_id, instalacion_id, generacion) DO NOTHING
            """;

    private static final String SQL_FINALIZAR_TRABAJO = """
            UPDATE trabajos_push
            SET estado = 'FINALIZADO',
                finalizado_en = CURRENT_TIMESTAMP,
                motivo_final = ?,
                reclamacion_id = NULL,
                reclamacion_vence_en = NULL
            WHERE id = ?
              AND estado = 'PENDIENTE_PREPARACION'
              AND reclamacion_id = ?
              AND reclamacion_vence_en > CURRENT_TIMESTAMP
            """;

    private static final String SQL_BLOQUEAR_PADRE = """
            SELECT id FROM trabajos_push WHERE id = ? AND estado = 'PREPARADO' FOR UPDATE
            """;

    private static final String SQL_ENTREGAS_PENDIENTES = """
            SELECT COUNT(*) FROM entregas_push WHERE trabajo_id = ? AND estado = 'PENDIENTE'
            """;

    private static final String SQL_CERRAR_PADRE = """
            UPDATE trabajos_push
            SET estado = 'FINALIZADO',
                finalizado_en = CURRENT_TIMESTAMP,
                motivo_final = ?
            WHERE id = ? AND estado = 'PREPARADO'
            """;

    private final JdbcTemplate jdbc;

    public ReclamacionTrabajoPushPersistenceAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public LoteReclamadoPush reclamar(int maximo, Duration vencimiento) {
        if (maximo < 1) {
            throw new IllegalArgumentException("El tamaño del lote debe ser positivo");
        }
        if (vencimiento == null || vencimiento.isZero() || vencimiento.isNegative()) {
            throw new IllegalArgumentException("La vigencia de la reclamación debe ser positiva");
        }
        double segundos = Math.max(1L, vencimiento.toSeconds());

        // La mitad del lote se reserva a la preparación; el cupo que no use se ofrece a las
        // entregas. Así ninguna de las dos colas espera indefinidamente a la otra.
        int cupoPreparacion = Math.max(1, maximo / 2);
        List<ReclamacionPush> lote = new ArrayList<>(maximo);
        List<UUID> preparaciones = jdbc.query(SQL_PREPARACIONES,
                (rs, fila) -> rs.getObject("id", UUID.class), cupoPreparacion);
        for (UUID trabajoId : preparaciones) {
            UUID reclamacionId = UUID.randomUUID();
            reclamarFila(SQL_RECLAMAR_PREPARACION, reclamacionId, segundos, trabajoId)
                    .ifPresent(venceEn -> lote.add(
                            ReclamacionPush.preparacion(trabajoId, reclamacionId, venceEn)));
        }

        int cupoEntregas = maximo - lote.size();
        boolean entregasLlenas = false;
        if (cupoEntregas > 0) {
            List<FilaEntrega> entregas = jdbc.query(SQL_ENTREGAS, this::mapearEntrega, cupoEntregas);
            for (FilaEntrega entrega : entregas) {
                UUID reclamacionId = UUID.randomUUID();
                reclamarFila(SQL_RECLAMAR_ENTREGA, reclamacionId, segundos, entrega.id())
                        .ifPresent(venceEn -> lote.add(ReclamacionPush.envio(entrega.id(),
                                entrega.trabajoId(), entrega.instalacionId(), entrega.generacion(),
                                reclamacionId, venceEn)));
            }
            entregasLlenas = entregas.size() == cupoEntregas;
        }

        if (lote.isEmpty()) {
            return LoteReclamadoPush.vacio();
        }
        notificar();
        boolean preparacionesLlenas = preparaciones.size() == cupoPreparacion;
        return new LoteReclamadoPush(lote, preparacionesLlenas || entregasLlenas);
    }

    @Override
    public boolean confirmarPreparacion(UUID trabajoId, UUID reclamacionId) {
        Objects.requireNonNull(trabajoId, "El trabajo es obligatorio");
        Objects.requireNonNull(reclamacionId, "La reclamación es obligatoria");
        int filas = jdbc.update(SQL_CONFIRMAR_PREPARACION, trabajoId, reclamacionId);
        if (filas > 0) {
            notificar();
        }
        return filas > 0;
    }

    @Override
    public boolean guardarResultadoEntrega(ResultadoEntregaPush resultado) {
        Objects.requireNonNull(resultado, "El resultado es obligatorio");
        boolean pendiente = resultado.estado() == EstadoEntregaPush.PENDIENTE;
        int filas = jdbc.update(SQL_RESULTADO_ENTREGA,
                resultado.estado().name(),
                resultado.intentos(),
                resultado.proximoIntentoEn() == null
                        ? null
                        : OffsetDateTime.ofInstant(resultado.proximoIntentoEn(), ZoneOffset.UTC),
                resultado.idMensajeFcm(),
                resultado.ultimoCodigoError(),
                pendiente,
                resultado.entregaId(),
                resultado.reclamacionId(),
                resultado.generacion());
        if (filas > 0) {
            notificar();
        }
        return filas > 0;
    }

    @Override
    public boolean liberarReclamacionDeEntrega(UUID entregaId, UUID reclamacionId) {
        Objects.requireNonNull(entregaId, "La entrega es obligatoria");
        Objects.requireNonNull(reclamacionId, "La reclamación es obligatoria");
        int filas = jdbc.update(SQL_LIBERAR_RECLAMACION_ENTREGA, entregaId, reclamacionId);
        if (filas > 0) {
            notificar();
        }
        return filas > 0;
    }

    @Override
    public Optional<Instant> proximoPlazo() {
        List<Instant> plazos = jdbc.query(SQL_PROXIMO_PLAZO, (rs, fila) -> {
            OffsetDateTime plazo = rs.getObject("plazo", OffsetDateTime.class);
            return plazo == null ? null : plazo.toInstant();
        });
        return plazos.stream().filter(Objects::nonNull).findFirst();
    }

    @Override
    public Optional<TrabajoPush> buscarTrabajo(UUID trabajoId) {
        List<TrabajoPush> filas = jdbc.query(SQL_TRABAJO, this::mapearTrabajo, trabajoId);
        return filas.stream().findFirst();
    }

    @Override
    public Optional<EntregaPush> buscarEntrega(UUID entregaId) {
        List<EntregaPush> filas = jdbc.query(SQL_ENTREGA, this::mapearEntregaDetalle, entregaId);
        return filas.stream().findFirst();
    }

    @Override
    public boolean trabajoExpirado(UUID trabajoId) {
        Objects.requireNonNull(trabajoId, "El trabajo es obligatorio");
        List<Boolean> expirado = jdbc.query(SQL_TRABAJO_EXPIRADO,
                (rs, fila) -> rs.getBoolean(1), trabajoId);
        return expirado.stream().findFirst().orElse(false);
    }

    @Override
    public void crearEntregas(UUID trabajoId, List<DestinoPush> destinos) {
        Objects.requireNonNull(trabajoId, "El trabajo de las entregas es obligatorio");
        Objects.requireNonNull(destinos, "Los destinos de las entregas son obligatorios");
        for (DestinoPush destino : destinos) {
            jdbc.update(SQL_CREAR_ENTREGA, UUID.randomUUID(), trabajoId,
                    destino.instalacionId(), destino.generacion());
        }
    }

    @Override
    public boolean finalizarTrabajo(UUID trabajoId, UUID reclamacionId, String motivo) {
        Objects.requireNonNull(trabajoId, "El trabajo es obligatorio");
        Objects.requireNonNull(reclamacionId, "La reclamación es obligatoria");
        int filas = jdbc.update(SQL_FINALIZAR_TRABAJO, motivo, trabajoId, reclamacionId);
        if (filas > 0) {
            notificar();
        }
        return filas > 0;
    }

    @Override
    public boolean cerrarPadreSiCorresponde(UUID trabajoId) {
        Objects.requireNonNull(trabajoId, "El trabajo es obligatorio");
        // El bloqueo serializa los resultados concurrentes del mismo padre: el segundo
        // espera a que el primero confirme y vuelve a contar las entregas pendientes.
        List<UUID> bloqueados = jdbc.query(SQL_BLOQUEAR_PADRE,
                (rs, fila) -> rs.getObject("id", UUID.class), trabajoId);
        if (bloqueados.isEmpty()) {
            return false;
        }
        Integer pendientes = jdbc.queryForObject(SQL_ENTREGAS_PENDIENTES, Integer.class, trabajoId);
        if (pendientes != null && pendientes > 0) {
            return false;
        }
        int filas = jdbc.update(SQL_CERRAR_PADRE,
                MotivoCierreTrabajoPush.ENTREGAS_TERMINALES.name(), trabajoId);
        if (filas > 0) {
            notificar();
        }
        return filas > 0;
    }

    private Optional<Instant> reclamarFila(String sql, UUID reclamacionId, double segundos,
                                           UUID identificador) {
        List<Instant> vencimientos = jdbc.query(sql,
                (rs, fila) -> rs.getObject("reclamacion_vence_en", OffsetDateTime.class).toInstant(),
                reclamacionId, segundos, identificador);
        return vencimientos.stream().findFirst();
    }

    private TrabajoPush mapearTrabajo(ResultSet rs, int fila) throws SQLException {
        return new TrabajoPush(rs.getObject("id", UUID.class),
                rs.getObject("notificacion_id", UUID.class),
                EstadoTrabajoPush.valueOf(rs.getString("estado")),
                rs.getObject("encolado_en", OffsetDateTime.class).toInstant(),
                rs.getObject("expira_en", OffsetDateTime.class).toInstant(),
                rs.getString("motivo_final"));
    }

    private EntregaPush mapearEntregaDetalle(ResultSet rs, int fila) throws SQLException {
        OffsetDateTime proximo = rs.getObject("proximo_intento_en", OffsetDateTime.class);
        return new EntregaPush(rs.getObject("id", UUID.class),
                rs.getObject("trabajo_id", UUID.class),
                rs.getObject("instalacion_id", UUID.class),
                rs.getLong("generacion"),
                EstadoEntregaPush.valueOf(rs.getString("estado")),
                rs.getInt("intentos"),
                proximo == null ? null : proximo.toInstant());
    }

    private FilaEntrega mapearEntrega(ResultSet rs, int fila) throws SQLException {
        return new FilaEntrega(rs.getObject("id", UUID.class),
                rs.getObject("trabajo_id", UUID.class),
                rs.getObject("instalacion_id", UUID.class),
                rs.getLong("generacion"));
    }

    private void notificar() {
        jdbc.execute("SELECT pg_notify('" + SenalTrabajoPush.CANAL + "', '')");
    }

    private record FilaEntrega(UUID id, UUID trabajoId, UUID instalacionId, long generacion) {}
}
