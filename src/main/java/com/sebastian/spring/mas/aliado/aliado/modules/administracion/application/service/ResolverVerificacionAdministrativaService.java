package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.ModeracionService;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.ResolverVerificacionAdministrativaUseCase;
import java.time.*;
import java.util.*;

public class ResolverVerificacionAdministrativaService
    implements ResolverVerificacionAdministrativaUseCase {
  private final ModeracionService operaciones;

  public ResolverVerificacionAdministrativaService(ModeracionService operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public void ejecutar(ResolverVerificacionAdministrativaCommand command) {
    operaciones.resolverVerificacion(
        command.solicitudId(),
        command.administradorId(),
        command.estado(),
        command.justificacion());
  }
}
