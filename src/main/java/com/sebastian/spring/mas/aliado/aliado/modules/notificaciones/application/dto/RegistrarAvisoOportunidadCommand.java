package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import java.util.UUID;

/**
 * Registro del aviso NUEVA_OPORTUNIDAD producido por una solicitud publicada
 * (SPEC-06/09, F09-PUSH-05). La entrega externa no forma parte de este caso de uso.
 */
public record RegistrarAvisoOportunidadCommand(UUID solicitudId, UUID prestadorId, int version,
                                               String detalle) {}
