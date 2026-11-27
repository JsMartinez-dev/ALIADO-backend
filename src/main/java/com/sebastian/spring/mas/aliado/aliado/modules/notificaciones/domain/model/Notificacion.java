package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Notificación unificada (plan general 6.2 y 19): un solo modelo para SPEC-06/07/09. */
public record Notificacion(UUID id, UUID destinatarioId, TipoNotificacion tipo, UUID aggregateId,
                           int eventVersion, String detalle, Canal canal, EstadoEntrega estadoEntrega,
                           int intentos, OffsetDateTime fechaGeneracion, OffsetDateTime fechaEntrega,
                           OffsetDateTime leidaEn) {}
