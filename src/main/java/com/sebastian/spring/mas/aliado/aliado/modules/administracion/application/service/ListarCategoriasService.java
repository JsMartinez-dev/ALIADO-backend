package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.ListarCategoriasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoAdminPort;
import java.time.*;
import java.util.*;

public class ListarCategoriasService implements ListarCategoriasUseCase {
  private final CatalogoAdminPort operaciones;

  public ListarCategoriasService(CatalogoAdminPort operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public List<CategoriaResponse> ejecutar() {
    var value = operaciones.listarCategorias();
    return value.stream()
        .map(v -> new CategoriaResponse(v.id(), v.nombre(), v.estado().name()))
        .toList();
  }
}
