package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.BloqueoCuentaService;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.LevantarBloqueoUseCase;
import java.time.*;
import java.util.*;

public class LevantarBloqueoService implements LevantarBloqueoUseCase {
  private final BloqueoCuentaService operaciones;

  public LevantarBloqueoService(BloqueoCuentaService operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public void ejecutar(LevantarBloqueoCommand command) {
    operaciones.levantarBloqueo(
        command.usuarioId(), command.bloqueoId(), command.administradorId());
  }
}
