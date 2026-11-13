package com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.SolicitarRecuperacionCommand;

public interface SolicitarRecuperacionAccesoUseCase {
    void ejecutar(SolicitarRecuperacionCommand command);
}
