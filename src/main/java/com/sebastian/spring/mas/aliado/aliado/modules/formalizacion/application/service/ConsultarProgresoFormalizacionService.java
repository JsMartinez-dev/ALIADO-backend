package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.FormalizacionService;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto.FormalizacionResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.usecase.ConsultarProgresoFormalizacionUseCase;
import java.util.*;

public class ConsultarProgresoFormalizacionService
    implements ConsultarProgresoFormalizacionUseCase {
  private final FormalizacionService operaciones;

  public ConsultarProgresoFormalizacionService(FormalizacionService operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public List<ProgresoResult> ejecutar(ConsultarProgresoFormalizacionCommand command) {
    return operaciones.progreso(command.prestadorId()).stream()
        .map(FormalizacionResult::progreso)
        .toList();
  }
}
