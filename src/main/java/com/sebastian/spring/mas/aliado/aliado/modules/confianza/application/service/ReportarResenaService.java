package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service;

import java.util.*;
import java.time.*;
import java.math.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase.ReportarResenaUseCase;

public class ReportarResenaService implements ReportarResenaUseCase {
    private final OperacionesConfianza operaciones;
    public ReportarResenaService(OperacionesConfianza operaciones) { this.operaciones = operaciones; }
    @Override
    public UUID ejecutar(ReportarResenaCommand command) {
        return operaciones.reportarResena(command.resenaId(), command.reportanteId(), command.motivo());
    }
}
