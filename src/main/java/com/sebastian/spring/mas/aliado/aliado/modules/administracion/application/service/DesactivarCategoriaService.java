package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.DesactivarCategoriaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoAdminPort;
import java.time.*;
import java.util.*;

public class DesactivarCategoriaService implements DesactivarCategoriaUseCase {
  private final CatalogoAdminPort operaciones;

  public DesactivarCategoriaService(CatalogoAdminPort operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public void ejecutar(DesactivarCategoriaCommand command) {
    operaciones.desactivarCategoria(command.administradorId(), command.id());
  }
}
