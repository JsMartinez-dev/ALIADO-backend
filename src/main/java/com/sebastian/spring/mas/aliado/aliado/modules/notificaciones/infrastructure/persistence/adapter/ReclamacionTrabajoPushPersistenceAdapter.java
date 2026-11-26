package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.persistence.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.LoteReclamadoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEntregaPush;
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
              AND e.proximo_intento_en <= CURRENT_TIMESTAMP
              AND (e.reclamacion_vence_en IS NULL OR e.reclamacion_vence_en <= CURRENT_TIMESTAMP)
              AND t.estado <> 'FINALIZADO'
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

    private static final String SQL_PROXIMO_PLAZO = """
            SELECT MIN(plazo) AS plazo
            FROM (
                SELECT reclamacion_vence_en AS plazo FROM trabajos_push
                WHERE reclamacion_vence_en IS NOT NULL
                UNION ALL
                SELECT reclamacion_vence_en FROM entregas_push
                WHERE reclamacion_vence_en IS NOT NULL
                UNION ALL
                SELECT proximo_intento_en FROM entregas_push
                WHERE estado = 'PENDIENTE'
                UNION ALL
                SELECT expira_en FROM trabajos_push
                WHERE estado <> 'FINALIZADO'
            ) plazos
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
    public Optional<Instant> proximoPlazo() {
        List<Instant> plazos = jdbc.query(SQL_PROXIMO_PLAZO, (rs, fila) -> {
            OffsetDateTime plazo = rs.getObject("plazo", OffsetDateTime.class);
            return plazo == null ? null : plazo.toInstant();
        });
        return plazos.stream().filter(Objects::nonNull).findFirst();
    }

    private Optional<Instant> reclamarFila(String sql, UUID reclamacionId, double segundos,
                                           UUID identificador) {
        List<Instant> vencimientos = jdbc.query(sql,
                (rs, fila) -> rs.getObject("reclamacion_vence_en", OffsetDateTime.class).toInstant(),
                reclamacionId, segundos, identificador);
        return vencimientos.stream().findFirst();
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
