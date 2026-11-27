package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.BloqueoCuentaService;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.BloquearCuentaUseCase;

public class BloquearCuentaService implements BloquearCuentaUseCase {
  private final BloqueoCuentaService bloqueoCuentaService;

  public BloquearCuentaService(BloqueoCuentaService bloqueoCuentaService) {
    this.bloqueoCuentaService = bloqueoCuentaService;
  }

  @Override
  public BloqueoResponse ejecutar(BloquearCuentaCommand command) {
    var value =
        bloqueoCuentaService.bloquearUsuario(
            command.usuarioId(), command.administradorId(), command.motivo());
    return new BloqueoResponse(value.id(), value.usuarioId(), value.motivo(), value.activo());
  }
}
