package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import java.util.UUID;

/**
 * Lectura individual de un aviso propio (F09-BANDEJA-02). El destinatario procede de la
 * sesión autenticada, nunca del cuerpo de la petición.
 */
public record MarcarAvisoLeidoCommand(UUID notificacionId, UUID destinatarioId) {}
