package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.ReporteModeracionService;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.RevisarReporteUseCase;
import java.time.*;
import java.util.*;

public class RevisarReporteService implements RevisarReporteUseCase {
  private final ReporteModeracionService operaciones;

  public RevisarReporteService(ReporteModeracionService operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public ReporteResponse ejecutar(RevisarReporteCommand command) {
    var value = operaciones.detalleReporte(command.reporteId(), command.administradorId());
    return reporte(value, operaciones.evidenciasDe(value.id()));
  }

  private ReporteResponse reporte(
      com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.model.Reporte r,
      List<com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.model.EvidenciaReporte>
          evidencias) {
    return new ReporteResponse(
        r.id(),
        r.tipo().name(),
        r.objetivoId(),
        r.motivo(),
        r.estado().name(),
        r.fechaCreacion(),
        r.detalleRevisadoEn(),
        evidencias.stream()
            .map(e -> new EvidenciaResumen(e.id(), e.tipoContenido(), e.fechaCarga()))
            .toList());
  }
}
