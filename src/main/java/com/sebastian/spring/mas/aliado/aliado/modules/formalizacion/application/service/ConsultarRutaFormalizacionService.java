package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.FormalizacionService;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto.FormalizacionResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.usecase.ConsultarRutaFormalizacionUseCase;
import java.util.*;

public class ConsultarRutaFormalizacionService implements ConsultarRutaFormalizacionUseCase {
  private final FormalizacionService operaciones;

  public ConsultarRutaFormalizacionService(FormalizacionService operaciones) {
    this.operaciones = operaciones;
  }

  @Override
  public List<RutaResult> ejecutar() {
    return operaciones.rutasActivas().stream()
        .map(
            r ->
                new RutaResult(
                    r.id(),
                    r.nombre(),
                    r.descripcion(),
                    operaciones.pasosDe(r.id()).stream()
                        .map(
                            p ->
                                new PasoResult(
                                    p.paso().id(),
                                    p.paso().titulo(),
                                    p.paso().descripcion(),
                                    p.paso().orden(),
                                    p.enlaces().stream().map(FormalizacionResult::enlace).toList()))
                        .toList(),
                    FormalizacionService.AVISO_LEGAL))
        .toList();
  }
}
