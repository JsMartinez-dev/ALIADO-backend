package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import java.util.UUID;

/**
 * Baja diferida sin sesión: solo la credencial de revocación del vínculo
 * (F09-PUSH-01/D-08). Nunca se acepta un Bearer anterior para esta operación.
 */
public record RevocarVinculoPushDiferidoCommand(UUID instalacionId, String credencialRevocacion) {}
