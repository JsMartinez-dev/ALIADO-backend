package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntrega;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Lectura de la bandeja de notificaciones propias (SPEC-09). */
public interface NotificacionConsultaPort {
    record NotificacionConsulta(UUID id, TipoNotificacion tipo, UUID servicioId, UUID solicitudId, UUID propuestaId,
                                String detalle, EstadoEntrega estadoEntrega, OffsetDateTime fechaGeneracion) {}

    List<NotificacionConsulta> listarPropias(UUID destinatarioId);
    List<NotificacionConsulta> listarPorTipo(UUID destinatarioId, TipoNotificacion tipo);
}
