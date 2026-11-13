package com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.SolicitarRecuperacionCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase.SolicitarRecuperacionAccesoUseCase;

public class SolicitarRecuperacionAccesoService implements SolicitarRecuperacionAccesoUseCase {
    private final AuthService authService;

    public SolicitarRecuperacionAccesoService(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void ejecutar(SolicitarRecuperacionCommand command) {
        authService.solicitarRecuperacion(command.email());
    }
}
