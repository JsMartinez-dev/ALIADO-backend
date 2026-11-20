package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;
import java.util.UUID;
public record ConsultarNotificacionesCommand(UUID destinatarioId, String tipo) {}
