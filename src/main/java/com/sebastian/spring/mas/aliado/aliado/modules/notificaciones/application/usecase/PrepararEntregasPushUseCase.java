package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.PrepararEntregasPushCommand;

/**
 * Prepara de forma idempotente las entregas de un trabajo padre reclamado
 * (F09-PUSH-08).
 */
public interface PrepararEntregasPushUseCase {

    void ejecutar(PrepararEntregasPushCommand command);
}
