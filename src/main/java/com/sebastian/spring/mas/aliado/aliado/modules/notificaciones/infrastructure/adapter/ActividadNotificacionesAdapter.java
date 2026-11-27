package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.NotificacionConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ActividadNotificacionesPort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adapta la bandeja de notificaciones a la vista mínima del historial de actividad. */
@Component
public class ActividadNotificacionesAdapter implements ActividadNotificacionesPort {
    private final NotificacionConsultaPort consultas;

    public ActividadNotificacionesAdapter(NotificacionConsultaPort consultas) {
        this.consultas = consultas;
    }

    @Override
    public List<ActividadNotificacion> listarPropias(UUID usuarioId) {
        return consultas.listarPropias(usuarioId).stream()
                .map(n -> new ActividadNotificacion(n.id(), n.tipo().name(), n.detalle(), n.fechaGeneracion()))
                .toList();
    }
}
