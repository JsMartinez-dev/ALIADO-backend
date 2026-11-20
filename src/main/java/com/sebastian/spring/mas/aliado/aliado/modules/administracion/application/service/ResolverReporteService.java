package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.ReporteModeracionService;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.ResolverReporteUseCase;
import java.time.*;
import java.util.*;

public class ResolverReporteService implements ResolverReporteUseCase {
  private final ReporteModeracionService operaciones;

  public ResolverReporteService(ReporteModeracionService operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public ReporteResponse ejecutar(ResolverReporteCommand command) {
    var value =
        operaciones.resolverReporte(
            command.reporteId(),
            command.administradorId(),
            command.resultado(),
            command.justificacion());
    return reporte(value, List.of());
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
