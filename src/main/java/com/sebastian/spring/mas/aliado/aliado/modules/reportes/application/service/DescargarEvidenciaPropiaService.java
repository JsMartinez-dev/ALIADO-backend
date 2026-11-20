package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.service;

import java.util.*;
import java.time.*;
import java.math.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.usecase.DescargarEvidenciaPropiaUseCase;

public class DescargarEvidenciaPropiaService implements DescargarEvidenciaPropiaUseCase {
    private final OperacionesReportes operaciones;
    public DescargarEvidenciaPropiaService(OperacionesReportes operaciones) { this.operaciones = operaciones; }
    @Override
    public EvidenciaContenidoResult ejecutar(DescargarEvidenciaPropiaCommand command) {
        var resultado = operaciones.descargarEvidenciaPropia(command.reporteId(), command.evidenciaId(), command.reportanteId());
        return new EvidenciaContenidoResult(resultado.contenido(), resultado.tipoContenido());
    }
}
