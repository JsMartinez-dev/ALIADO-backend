package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.FormalizacionService;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto.FormalizacionResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.usecase.ActualizarProgresoFormalizacionUseCase;
import java.util.*;

public class ActualizarProgresoFormalizacionService
    implements ActualizarProgresoFormalizacionUseCase {
  private final FormalizacionService operaciones;

  public ActualizarProgresoFormalizacionService(FormalizacionService operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public ProgresoResult ejecutar(ActualizarProgresoFormalizacionCommand command) {
    return FormalizacionResult.progreso(
        operaciones.actualizarProgreso(command.prestadorId(), command.rutaId(), command.valor()));
  }
}
