package com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.ConfirmarRecuperacionCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase.ConfirmarRecuperacionAccesoUseCase;

public class ConfirmarRecuperacionAccesoService implements ConfirmarRecuperacionAccesoUseCase {
    private final AuthService authService;

    public ConfirmarRecuperacionAccesoService(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void ejecutar(ConfirmarRecuperacionCommand command) {
        authService.confirmarRecuperacion(command.token(), command.nuevaPassword());
    }
}
