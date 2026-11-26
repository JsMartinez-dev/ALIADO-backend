package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RegistrarAvisoOportunidadCommand;

/**
 * Registra el aviso de oportunidad en la bandeja y encola su trabajo push cuando el
 * destinatario tiene el aviso habilitado (F09-PUSH-05, plan de notificaciones push).
 */
public interface RegistrarAvisoOportunidadUseCase {
    void ejecutar(RegistrarAvisoOportunidadCommand command);
}
