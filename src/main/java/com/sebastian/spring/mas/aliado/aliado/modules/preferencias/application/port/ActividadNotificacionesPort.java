package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Vista mínima de notificaciones que necesita el historial de actividad
 * (SPEC-14): solo tipo, detalle y fecha. El módulo de preferencias no depende
 * del contrato completo de consulta de notificaciones.
 */
public interface ActividadNotificacionesPort {
    record ActividadNotificacion(UUID id, String tipo, String detalle, OffsetDateTime fechaGeneracion) {}

    List<ActividadNotificacion> listarPropias(UUID usuarioId);
}
