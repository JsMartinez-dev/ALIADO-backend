package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.EditarCategoriaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoAdminPort;
import java.time.*;
import java.util.*;

public class EditarCategoriaService implements EditarCategoriaUseCase {
  private final CatalogoAdminPort operaciones;

  public EditarCategoriaService(CatalogoAdminPort operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public CategoriaResponse ejecutar(EditarCategoriaCommand command) {
    var value =
        operaciones.actualizarCategoria(
            command.administradorId(), command.id(), command.nombre(), command.estado());
    return new CategoriaResponse(value.id(), value.nombre(), value.estado().name());
  }
}
