package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.persistence.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Canal;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntrega;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoLectura;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.persistence.jpa.NotificacionJpaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.persistence.mapper.NotificacionPersistenceMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificacionPersistenceAdapter implements NotificacionRepository {
    private static final String SQL_INSERTAR_AVISO = """
            INSERT INTO notificaciones (id, destinatario_id, tipo, aggregate_id, event_version, detalle,
                                        canal, estado_entrega, intentos, fecha_generacion, leida_en)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (tipo, aggregate_id, destinatario_id, event_version) DO NOTHING
            """;

    /**
     * Una sola sentencia resuelve pertenencia y primera lectura: filtra por identificador
     * y destinatario y conserva la fecha ya guardada. Cero filas significa aviso
     * inexistente o ajeno, sin una consulta de existencia previa.
     */
    private static final String SQL_MARCAR_LEIDA = """
            UPDATE notificaciones
               SET leida_en = COALESCE(leida_en, ?)
             WHERE id = ? AND destinatario_id = ?
            """;

    private static final String SQL_MARCAR_TODAS_LAS_LEIDAS = """
            UPDATE notificaciones
               SET leida_en = ?
             WHERE destinatario_id = ? AND leida_en IS NULL
            """;

    private final NotificacionJpaRepository jpa;
    private final JdbcTemplate jdbc;

    public NotificacionPersistenceAdapter(NotificacionJpaRepository jpa, JdbcTemplate jdbc) {
        this.jpa = jpa;
        this.jdbc = jdbc;
    }

    @Override
    public boolean existeEvento(String tipo, UUID aggregateId, UUID destinatarioId, int eventVersion) {
        return jpa.existsByTipoAndAggregateIdAndDestinatarioIdAndEventVersion(tipo, aggregateId, destinatarioId, eventVersion);
    }

    @Override
    public List<Notificacion> porDestinatario(UUID destinatarioId) {
        return jpa.findByDestinatarioIdOrderByFechaGeneracionDesc(destinatarioId).stream()
                .map(NotificacionPersistenceMapper::aDominio).toList();
    }

    @Override
    public List<Notificacion> porDestinatarioYTipo(UUID destinatarioId, String tipo) {
        return jpa.findByDestinatarioIdAndTipoOrderByFechaGeneracionDesc(destinatarioId, tipo).stream()
                .map(NotificacionPersistenceMapper::aDominio).toList();
    }

    @Override
    public Optional<Notificacion> porId(UUID id) {
        List<Notificacion> filas = jdbc.query("""
                SELECT id, destinatario_id, tipo, aggregate_id, event_version, detalle, canal,
                       estado_entrega, intentos, fecha_generacion, fecha_entrega, leida_en
                FROM notificaciones WHERE id = ?
                """, this::mapearNotificacion, id);
        return filas.stream().findFirst();
    }

    @Override
    public Notificacion guardar(Notificacion notificacion) {
        return NotificacionPersistenceMapper.aDominio(jpa.save(NotificacionPersistenceMapper.aEntidad(notificacion)));
    }

    @Override
    public boolean insertarSiNoExiste(Notificacion notificacion) {
        int creados = jdbc.update(SQL_INSERTAR_AVISO,
                notificacion.id(), notificacion.destinatarioId(), notificacion.tipo().name(),
                notificacion.aggregateId(), notificacion.eventVersion(), notificacion.detalle(),
                notificacion.canal().name(), notificacion.estadoEntrega().name(), notificacion.intentos(),
                notificacion.fechaGeneracion(), notificacion.leidaEn());
        return creados == 1;
    }

    @Override
    public boolean marcarLeida(UUID id, UUID destinatarioId, OffsetDateTime leidaEn) {
        // PostgreSQL cuenta la fila aunque COALESCE conserve la fecha ya guardada, así que
        // un aviso propio ya leído se distingue de un aviso ajeno o inexistente sin una
        // consulta de existencia adicional.
        return jdbc.update(SQL_MARCAR_LEIDA, leidaEn, id, destinatarioId) == 1;
    }

    @Override
    public int marcarTodasLeidas(UUID destinatarioId, OffsetDateTime leidaEn) {
        return jdbc.update(SQL_MARCAR_TODAS_LAS_LEIDAS, leidaEn, destinatarioId);
    }

    /**
     * Página de la bandeja (F09-BANDEJA-03) en una sola consulta. El par fecha–ID se compara
     * lexicográficamente con la fila de comparación de PostgreSQL, que usa el índice
     * {@code (destinatario_id, fecha_generacion DESC, id DESC)} de V10. El destinatario
     * siempre viene de la sesión y nunca del cursor.
     */
    @Override
    public List<Notificacion> buscarBandeja(UUID destinatarioId, EstadoLectura estadoLectura,
                                            TipoNotificacion tipo, OffsetDateTime antesDeFecha,
                                            UUID antesDeId, int limite) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, destinatario_id, tipo, aggregate_id, event_version, detalle, canal,
                       estado_entrega, intentos, fecha_generacion, fecha_entrega, leida_en
                  FROM notificaciones
                 WHERE destinatario_id = ?
                """);
        List<Object> argumentos = new ArrayList<>();
        argumentos.add(destinatarioId);
        if (estadoLectura == EstadoLectura.NO_LEIDA) {
            sql.append(" AND leida_en IS NULL");
        } else if (estadoLectura == EstadoLectura.LEIDA) {
            sql.append(" AND leida_en IS NOT NULL");
        }
        if (tipo != null) {
            sql.append(" AND tipo = ?");
            argumentos.add(tipo.name());
        }
        if (antesDeFecha != null && antesDeId != null) {
            sql.append(" AND (fecha_generacion, id) < (?, ?)");
            argumentos.add(antesDeFecha);
            argumentos.add(antesDeId);
        }
        sql.append(" ORDER BY fecha_generacion DESC, id DESC LIMIT ?");
        argumentos.add(limite);
        return jdbc.query(sql.toString(), this::mapearNotificacion, argumentos.toArray());
    }

    @Override
    public boolean hayNoLeidas(UUID destinatarioId) {
        Boolean existe = jdbc.queryForObject("""
                SELECT EXISTS (SELECT 1 FROM notificaciones
                                WHERE destinatario_id = ? AND leida_en IS NULL)
                """, Boolean.class, destinatarioId);
        return Boolean.TRUE.equals(existe);
    }

    private Notificacion mapearNotificacion(ResultSet rs, int fila) throws SQLException {
        return new Notificacion(rs.getObject("id", UUID.class),
                rs.getObject("destinatario_id", UUID.class),
                TipoNotificacion.valueOf(rs.getString("tipo")),
                rs.getObject("aggregate_id", UUID.class),
                rs.getInt("event_version"),
                rs.getString("detalle"),
                Canal.valueOf(rs.getString("canal")),
                EstadoEntrega.valueOf(rs.getString("estado_entrega")),
                rs.getInt("intentos"),
                rs.getObject("fecha_generacion", OffsetDateTime.class),
                rs.getObject("fecha_entrega", OffsetDateTime.class),
                rs.getObject("leida_en", OffsetDateTime.class));
    }
}
