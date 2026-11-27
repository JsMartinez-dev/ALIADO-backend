package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RevocarVinculoPushDiferidoCommand;

public interface RevocarVinculoPushDiferidoUseCase {
    void ejecutar(RevocarVinculoPushDiferidoCommand command);
}
