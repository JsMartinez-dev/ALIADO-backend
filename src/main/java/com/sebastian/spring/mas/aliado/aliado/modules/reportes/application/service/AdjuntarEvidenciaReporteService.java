package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.service;

import java.util.*;
import java.time.*;
import java.math.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.usecase.AdjuntarEvidenciaReporteUseCase;

public class AdjuntarEvidenciaReporteService implements AdjuntarEvidenciaReporteUseCase {
    private final OperacionesReportes operaciones;
    public AdjuntarEvidenciaReporteService(OperacionesReportes operaciones) { this.operaciones = operaciones; }
    @Override
    public EvidenciaResult ejecutar(AdjuntarEvidenciaReporteCommand command) {
        var resultado = operaciones.agregarEvidencia(command.reporteId(), command.reportanteId(), command.nombreOriginal(), command.tipoContenido(), command.contenido());
        return new EvidenciaResult(resultado.id(), resultado.archivoKey(), resultado.tipoContenido());
    }
}
