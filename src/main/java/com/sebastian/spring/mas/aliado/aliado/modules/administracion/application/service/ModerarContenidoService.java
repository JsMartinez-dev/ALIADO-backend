package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.ModeracionService;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.ModerarContenidoUseCase;
import java.time.*;
import java.util.*;

public class ModerarContenidoService implements ModerarContenidoUseCase {
  private final ModeracionService operaciones;

  public ModerarContenidoService(ModeracionService operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public void ejecutar(ModerarContenidoCommand command) {
    operaciones.moderar(
        command.administradorId(),
        command.objetivoTipo(),
        command.objetivoId(),
        command.accion(),
        command.motivo());
  }
}
