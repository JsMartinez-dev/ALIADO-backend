package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.persistence.mapper;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Canal;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntrega;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.persistence.entity.NotificacionEntity;

public final class NotificacionPersistenceMapper {
    private NotificacionPersistenceMapper() {}

    public static Notificacion aDominio(NotificacionEntity e) {
        return new Notificacion(e.getId(), e.getDestinatarioId(), TipoNotificacion.valueOf(e.getTipo()),
                e.getAggregateId(), e.getEventVersion(), e.getDetalle(), Canal.valueOf(e.getCanal()),
                EstadoEntrega.valueOf(e.getEstadoEntrega()), e.getIntentos(), e.getFechaGeneracion(),
                e.getFechaEntrega(), e.getLeidaEn());
    }

    public static NotificacionEntity aEntidad(Notificacion n) {
        NotificacionEntity e = new NotificacionEntity();
        e.setId(n.id());
        e.setDestinatarioId(n.destinatarioId());
        e.setTipo(n.tipo().name());
        e.setAggregateId(n.aggregateId());
        e.setEventVersion(n.eventVersion());
        e.setDetalle(n.detalle());
        e.setCanal(n.canal().name());
        e.setEstadoEntrega(n.estadoEntrega().name());
        e.setIntentos(n.intentos());
        e.setFechaGeneracion(n.fechaGeneracion());
        e.setFechaEntrega(n.fechaEntrega());
        e.setLeidaEn(n.leidaEn());
        return e;
    }
}
