package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.CrearCategoriaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoAdminPort;
import java.time.*;
import java.util.*;

public class CrearCategoriaService implements CrearCategoriaUseCase {
  private final CatalogoAdminPort operaciones;

  public CrearCategoriaService(CatalogoAdminPort operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public CategoriaResponse ejecutar(CrearCategoriaCommand command) {
    var value = operaciones.crearCategoria(command.administradorId(), command.nombre());
    return new CategoriaResponse(value.id(), value.nombre(), value.estado().name());
  }
}
