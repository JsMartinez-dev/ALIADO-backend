package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.ListarZonasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoAdminPort;
import java.time.*;
import java.util.*;

public class ListarZonasService implements ListarZonasUseCase {
  private final CatalogoAdminPort operaciones;

  public ListarZonasService(CatalogoAdminPort operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public List<ZonaResponse> ejecutar() {
    var value = operaciones.listarZonas();
    return value.stream()
        .map(v -> new ZonaResponse(v.id(), v.nombre(), v.coberturaGeografica(), v.estado().name()))
        .toList();
  }
}
