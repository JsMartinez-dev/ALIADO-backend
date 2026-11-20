package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.CrearZonaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoAdminPort;
import java.time.*;
import java.util.*;

public class CrearZonaService implements CrearZonaUseCase {
  private final CatalogoAdminPort operaciones;

  public CrearZonaService(CatalogoAdminPort operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public ZonaResponse ejecutar(CrearZonaCommand command) {
    var value =
        operaciones.crearZona(
            command.administradorId(), command.nombre(), command.coberturaGeografica());
    return new ZonaResponse(
        value.id(), value.nombre(), value.coberturaGeografica(), value.estado().name());
  }
}
