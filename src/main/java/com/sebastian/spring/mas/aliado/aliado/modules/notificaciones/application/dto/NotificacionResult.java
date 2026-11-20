package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;
import java.util.UUID;
import java.time.OffsetDateTime;
public record NotificacionResult(UUID id, String tipo, UUID servicioId, UUID solicitudId,
        String detalle, String estadoEntrega, OffsetDateTime fechaGeneracion) {}
