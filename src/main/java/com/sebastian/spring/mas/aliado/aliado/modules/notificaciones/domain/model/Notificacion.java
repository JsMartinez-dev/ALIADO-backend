package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Notificacion(UUID id, UUID destinatarioId, TipoNotificacion tipo, UUID aggregateId,
                           int eventVersion, String detalle, Canal canal, EstadoEntrega estadoEntrega,
                           int intentos, OffsetDateTime fechaGeneracion, OffsetDateTime fechaEntrega) {}
