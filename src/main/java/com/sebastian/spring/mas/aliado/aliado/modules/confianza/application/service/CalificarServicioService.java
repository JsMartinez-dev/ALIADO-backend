package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase.CalificarServicioUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase.RecalcularReputacionUseCase;

public class CalificarServicioService implements CalificarServicioUseCase {
    private final OperacionesConfianza operaciones;
    private final RecalcularReputacionUseCase recalcularReputacionUseCase;
    public CalificarServicioService(OperacionesConfianza operaciones, RecalcularReputacionUseCase recalcularReputacionUseCase) {
        this.operaciones = operaciones;
        this.recalcularReputacionUseCase = recalcularReputacionUseCase;
    }
    @Override
    public CalificacionResult ejecutar(CalificarServicioCommand command) {
        var resultado = operaciones.registrarCalificacion(command.servicioId(), command.clienteId(), command.valor());
        recalcularReputacionUseCase.ejecutar(new RecalcularReputacionCommand(resultado.prestadorId()));
        return new CalificacionResult(resultado.id(), operaciones.confianzaDe(resultado.prestadorId()).reputacion().valor());
    }
}
