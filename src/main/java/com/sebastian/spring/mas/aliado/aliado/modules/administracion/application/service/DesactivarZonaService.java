package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.DesactivarZonaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoAdminPort;
import java.time.*;
import java.util.*;

public class DesactivarZonaService implements DesactivarZonaUseCase {
  private final CatalogoAdminPort operaciones;

  public DesactivarZonaService(CatalogoAdminPort operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public void ejecutar(DesactivarZonaCommand command) {
    operaciones.desactivarZona(command.administradorId(), command.id());
  }
}
