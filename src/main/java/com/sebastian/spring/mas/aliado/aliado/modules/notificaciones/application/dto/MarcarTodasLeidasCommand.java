package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import java.util.UUID;

/**
 * Lectura masiva de los avisos pendientes de una cuenta (F09-BANDEJA-02). El destinatario
 * procede de la sesión autenticada.
 */
public record MarcarTodasLeidasCommand(UUID destinatarioId) {}
