package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.RiesgoAuditoriaService;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.MarcarServicioAltoRiesgoUseCase;
import java.time.*;
import java.util.*;

public class MarcarServicioAltoRiesgoService implements MarcarServicioAltoRiesgoUseCase {
  private final RiesgoAuditoriaService operaciones;

  public MarcarServicioAltoRiesgoService(RiesgoAuditoriaService operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public AltoRiesgoResponse ejecutar(MarcarServicioAltoRiesgoCommand command) {
    var value =
        operaciones.marcarAltoRiesgo(
            command.servicioId(), command.administradorId(), command.criterio());
    return new AltoRiesgoResponse(value.id(), value.servicioId(), value.criterio(), value.activa());
  }
}
