package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RetirarDispositivoPushCommand;

public interface RetirarDispositivoPushUseCase {
    void ejecutar(RetirarDispositivoPushCommand command);
}
