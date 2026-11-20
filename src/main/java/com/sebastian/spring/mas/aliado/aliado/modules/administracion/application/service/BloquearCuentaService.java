package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.BloqueoCuentaService;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.BloquearCuentaUseCase;
import java.time.*;
import java.util.*;

public class BloquearCuentaService implements BloquearCuentaUseCase {
  private final BloqueoCuentaService operaciones;

  public BloquearCuentaService(BloqueoCuentaService operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public BloqueoResponse ejecutar(BloquearCuentaCommand command) {
    var value =
        operaciones.bloquearUsuario(
            command.usuarioId(), command.administradorId(), command.motivo());
    return new BloqueoResponse(value.id(), value.usuarioId(), value.motivo(), value.activo());
  }
}
