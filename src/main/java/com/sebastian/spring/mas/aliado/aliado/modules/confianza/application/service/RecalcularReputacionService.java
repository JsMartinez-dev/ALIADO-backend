package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service;

import java.util.*;
import java.time.*;
import java.math.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase.RecalcularReputacionUseCase;

public class RecalcularReputacionService implements RecalcularReputacionUseCase {
    private final OperacionesConfianza operaciones;
    public RecalcularReputacionService(OperacionesConfianza operaciones) { this.operaciones = operaciones; }
    @Override
    public void ejecutar(RecalcularReputacionCommand command) {
        operaciones.recalcularReputacion(command.prestadorId());
    }
}
