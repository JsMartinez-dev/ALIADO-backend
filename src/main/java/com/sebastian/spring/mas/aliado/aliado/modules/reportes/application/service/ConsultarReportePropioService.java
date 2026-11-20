package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.service;

import java.util.*;
import java.time.*;
import java.math.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.usecase.ConsultarReportePropioUseCase;

public class ConsultarReportePropioService implements ConsultarReportePropioUseCase {
    private final OperacionesReportes operaciones;
    public ConsultarReportePropioService(OperacionesReportes operaciones) { this.operaciones = operaciones; }
    @Override
    public ReporteResult ejecutar(ConsultarReportePropioCommand command) {
        var resultado = operaciones.miReporte(command.reporteId(), command.reportanteId());
        return new ReporteResult(resultado.id(), resultado.tipo().name(), resultado.objetivoId(), resultado.motivo(), resultado.estado().name(), resultado.fechaCreacion(), operaciones.evidenciasDe(resultado.id(), command.reportanteId()).stream().map(e -> new EvidenciaResult(e.id(), e.archivoKey(), e.tipoContenido())).toList());
    }
}
