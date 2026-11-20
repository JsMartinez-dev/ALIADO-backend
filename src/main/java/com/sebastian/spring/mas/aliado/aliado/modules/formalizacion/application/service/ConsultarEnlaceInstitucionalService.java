package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.FormalizacionService;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto.FormalizacionResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.usecase.ConsultarEnlaceInstitucionalUseCase;
import java.util.*;

public class ConsultarEnlaceInstitucionalService implements ConsultarEnlaceInstitucionalUseCase {
  private final FormalizacionService operaciones;

  public ConsultarEnlaceInstitucionalService(FormalizacionService operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public EnlaceResult ejecutar(ConsultarEnlaceInstitucionalCommand command) {
    return FormalizacionResult.enlace(operaciones.enlace(command.enlaceId()));
  }
}
