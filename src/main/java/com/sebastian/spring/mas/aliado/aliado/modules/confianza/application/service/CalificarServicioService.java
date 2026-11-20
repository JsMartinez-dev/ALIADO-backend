package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service;

import java.util.*;
import java.time.*;
import java.math.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase.CalificarServicioUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase.RecalcularReputacionUseCase;

public class CalificarServicioService implements CalificarServicioUseCase {
    private final OperacionesConfianza operaciones;
    private final RecalcularReputacionUseCase recalcular;
    public CalificarServicioService(OperacionesConfianza operaciones, RecalcularReputacionUseCase recalcular) {
        this.operaciones = operaciones;
        this.recalcular = recalcular;
    }
    @Override
    public CalificacionResult ejecutar(CalificarServicioCommand command) {
        var resultado = operaciones.registrarCalificacion(command.servicioId(), command.clienteId(), command.valor());
        recalcular.ejecutar(new RecalcularReputacionCommand(resultado.prestadorId()));
        return new CalificacionResult(resultado.id(), operaciones.confianzaDe(resultado.prestadorId()).reputacion().valor());
    }
}
