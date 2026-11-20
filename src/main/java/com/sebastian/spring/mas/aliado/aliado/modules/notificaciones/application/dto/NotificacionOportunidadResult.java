package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;
import java.time.OffsetDateTime;
import java.util.UUID;
public record NotificacionOportunidadResult(UUID id,UUID solicitudId,String detalle,String estadoEntrega,
                                           OffsetDateTime fechaGeneracion) {}
