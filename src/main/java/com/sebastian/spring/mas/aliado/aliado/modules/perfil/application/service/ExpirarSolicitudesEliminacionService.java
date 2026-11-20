package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ExpirarSolicitudesEliminacionUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.SolicitudEliminacionRepository;
import java.time.OffsetDateTime;

public class ExpirarSolicitudesEliminacionService implements ExpirarSolicitudesEliminacionUseCase {
    private final SolicitudEliminacionRepository eliminaciones;

    public ExpirarSolicitudesEliminacionService(SolicitudEliminacionRepository eliminaciones) {
        this.eliminaciones = eliminaciones;
    }

    @Override
    public void ejecutar() {
        eliminaciones.expirarVencidas(OffsetDateTime.now());
    }
}
