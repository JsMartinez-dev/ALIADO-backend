package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.ReporteModeracionService;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.DescargarEvidenciaAdministrativaUseCase;
import java.time.*;
import java.util.*;

public class DescargarEvidenciaAdministrativaService
    implements DescargarEvidenciaAdministrativaUseCase {
  private final ReporteModeracionService operaciones;

  public DescargarEvidenciaAdministrativaService(ReporteModeracionService operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public EvidenciaContenidoResult ejecutar(DescargarEvidenciaAdministrativaCommand command) {
    var value = operaciones.descargarEvidencia(command.reporteId(), command.evidenciaId());
    return new EvidenciaContenidoResult(value.contenido(), value.tipoContenido());
  }
}
