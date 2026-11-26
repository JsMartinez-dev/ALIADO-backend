package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.persistence.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.persistence.jpa.NotificacionJpaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.persistence.mapper.NotificacionPersistenceMapper;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificacionPersistenceAdapter implements NotificacionRepository {
    private static final String SQL_INSERTAR_AVISO = """
            INSERT INTO notificaciones (id, destinatario_id, tipo, aggregate_id, event_version, detalle,
                                        canal, estado_entrega, intentos, fecha_generacion)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (tipo, aggregate_id, destinatario_id, event_version) DO NOTHING
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
    public List<Notificacion> pendientesParaEntrega() {
        return jpa.findPendientesParaEntrega().stream().map(NotificacionPersistenceMapper::aDominio).toList();
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
                notificacion.fechaGeneracion());
        return creados == 1;
    }
}
