package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.FormalizacionService;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto.FormalizacionResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.usecase.ActualizarEnlaceInstitucionalUseCase;
import java.util.*;

public class ActualizarEnlaceInstitucionalService implements ActualizarEnlaceInstitucionalUseCase {
  private final FormalizacionService operaciones;

  public ActualizarEnlaceInstitucionalService(FormalizacionService operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public EnlaceResult ejecutar(ActualizarEnlaceInstitucionalCommand command) {
    return FormalizacionResult.enlace(
        operaciones.actualizarEnlace(
            command.enlaceId(), command.nombre(), command.url(), command.estado()));
  }
}
