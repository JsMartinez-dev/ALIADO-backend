package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RegistrarDispositivoPushCommand;

public interface RegistrarDispositivoPushUseCase {
    void ejecutar(RegistrarDispositivoPushCommand command);
}
