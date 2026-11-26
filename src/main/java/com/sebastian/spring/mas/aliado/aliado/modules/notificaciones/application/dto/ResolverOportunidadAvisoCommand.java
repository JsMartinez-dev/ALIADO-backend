package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import java.util.UUID;

/** Aviso y destinatario autenticado que resuelven el estado de la oportunidad (F09-PUSH-10). */
public record ResolverOportunidadAvisoCommand(UUID notificacionId, UUID prestadorId) {}
