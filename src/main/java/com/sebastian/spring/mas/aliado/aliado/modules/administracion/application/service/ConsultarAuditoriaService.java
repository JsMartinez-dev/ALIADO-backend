package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.RiesgoAuditoriaService;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.ConsultarAuditoriaUseCase;
import java.time.*;
import java.util.*;

public class ConsultarAuditoriaService implements ConsultarAuditoriaUseCase {
  private final RiesgoAuditoriaService operaciones;

  public ConsultarAuditoriaService(RiesgoAuditoriaService operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public List<AuditoriaResponse> ejecutar(ConsultarAuditoriaCommand command) {
    var value = operaciones.listarAuditoria(command.limite());
    return value.stream()
        .map(
            v ->
                new AuditoriaResponse(
                    v.id(),
                    v.accion(),
                    v.objetivoTipo(),
                    v.objetivoId(),
                    v.detalle(),
                    v.fecha(),
                    v.traceId()))
        .toList();
  }
}
