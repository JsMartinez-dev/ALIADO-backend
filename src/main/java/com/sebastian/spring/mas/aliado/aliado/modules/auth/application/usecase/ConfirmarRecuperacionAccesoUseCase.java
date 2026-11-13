package com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.ConfirmarRecuperacionCommand;

public interface ConfirmarRecuperacionAccesoUseCase {
    void ejecutar(ConfirmarRecuperacionCommand command);
}
