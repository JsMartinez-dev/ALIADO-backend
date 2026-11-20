package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.service;

import java.util.*;
import java.time.*;
import java.math.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.usecase.ReportarServicioUseCase;

public class ReportarServicioService implements ReportarServicioUseCase {
    private final OperacionesReportes operaciones;
    public ReportarServicioService(OperacionesReportes operaciones) { this.operaciones = operaciones; }
    @Override
    public UUID ejecutar(ReportarServicioCommand command) {
        return operaciones.crearReporteServicio(command.objetivoId(), command.reportanteId(), command.motivo());
    }
}
