package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.BusquedaAdminService;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.BuscarCuentasPerfilesUseCase;
import java.time.*;
import java.util.*;

public class BuscarCuentasPerfilesService implements BuscarCuentasPerfilesUseCase {
  private final BusquedaAdminService operaciones;

  public BuscarCuentasPerfilesService(BusquedaAdminService operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public BusquedaResponse ejecutar(BuscarCuentasPerfilesCommand command) {
    var value = operaciones.buscar(command.texto());
    return new BusquedaResponse(
        value.usuarios().stream().map(u -> new UsuarioResumen(u.id(), u.email(), u.rol())).toList(),
        value.solicitudes().stream()
            .map(u -> new SolicitudResumen(u.id(), u.descripcion(), u.estado()))
            .toList());
  }
}
