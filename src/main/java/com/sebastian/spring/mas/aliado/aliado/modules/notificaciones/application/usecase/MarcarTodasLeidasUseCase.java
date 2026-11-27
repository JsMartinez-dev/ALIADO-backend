package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.MarcarTodasLeidasCommand;

/**
 * Marca como leídos todos los avisos pendientes de una cuenta (F09-BANDEJA-02).
 */
public interface MarcarTodasLeidasUseCase {

    void ejecutar(MarcarTodasLeidasCommand command);
}
