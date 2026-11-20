package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.exception.FormalizacionOperacionException;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.exception.FormalizacionOperacionException.Tipo;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.usecase.ConsultarVinculacionInstitucionalUseCase;
import java.util.function.BooleanSupplier;

public class ConsultarVinculacionInstitucionalService
    implements ConsultarVinculacionInstitucionalUseCase {
  private final BooleanSupplier habilitada;

  public ConsultarVinculacionInstitucionalService(BooleanSupplier habilitada) {
    this.habilitada = habilitada;
  }

  @Override
  public void ejecutar() {
    if (!habilitada.getAsBoolean()) {
      throw new FormalizacionOperacionException(
          Tipo.NO_ENCONTRADO,
          "FUNCIONALIDAD_DESHABILITADA",
          "La vinculación institucional (UC080) es una función futura y no está disponible.");
    }
    throw new FormalizacionOperacionException(
        Tipo.NO_IMPLEMENTADO,
        "NO_IMPLEMENTADO",
        "La vinculación institucional está habilitada pero aún no implementada.");
  }
}
