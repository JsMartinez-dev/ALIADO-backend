package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.PerfilProfesionalNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ConsultarPerfilPropioUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
import java.util.UUID;

public class ConsultarPerfilPropioService implements ConsultarPerfilPropioUseCase {
    private final PerfilProfesionalRepository perfiles;

    public ConsultarPerfilPropioService(PerfilProfesionalRepository perfiles) {
        this.perfiles = perfiles;
    }

    @Override
    public PerfilProfesionalResult ejecutar(UUID prestadorId) {
        return perfiles.porPrestadorId(prestadorId)
                .map(PerfilProfesionalResult::desde)
                .orElseThrow(PerfilProfesionalNoEncontradoException::new);
    }
}
