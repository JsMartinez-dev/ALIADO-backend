package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.EditarZonaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoAdminPort;
import java.time.*;
import java.util.*;

public class EditarZonaService implements EditarZonaUseCase {
  private final CatalogoAdminPort operaciones;

  public EditarZonaService(CatalogoAdminPort operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public ZonaResponse ejecutar(EditarZonaCommand command) {
    var value =
        operaciones.actualizarZona(
            command.administradorId(),
            command.id(),
            command.nombre(),
            command.coberturaGeografica(),
            command.estado());
    return new ZonaResponse(
        value.id(), value.nombre(), value.coberturaGeografica(), value.estado().name());
  }
}
