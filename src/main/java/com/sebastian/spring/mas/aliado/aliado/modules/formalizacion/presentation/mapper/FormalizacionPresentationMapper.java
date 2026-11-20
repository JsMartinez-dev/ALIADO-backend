package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.presentation.mapper;

import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto.FormalizacionResult.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.presentation.dto.FormalizacionDtos;

public final class FormalizacionPresentationMapper {
  private FormalizacionPresentationMapper() {}

  public static FormalizacionDtos.EnlaceResponse aResponse(EnlaceResult e) {
    return new FormalizacionDtos.EnlaceResponse(e.id(), e.nombre(), e.url(), e.estado());
  }

  public static FormalizacionDtos.ProgresoResponse aResponse(ProgresoResult p) {
    return new FormalizacionDtos.ProgresoResponse(p.rutaId(), p.valorOrientativo(), p.avisoLegal());
  }

  public static FormalizacionDtos.RutaResponse aResponse(RutaResult r) {
    return new FormalizacionDtos.RutaResponse(
        r.id(),
        r.nombre(),
        r.descripcion(),
        r.pasos().stream()
            .map(
                p ->
                    new FormalizacionDtos.PasoResponse(
                        p.id(),
                        p.titulo(),
                        p.descripcion(),
                        p.orden(),
                        p.enlaces().stream()
                            .map(FormalizacionPresentationMapper::aResponse)
                            .toList()))
            .toList(),
        r.avisoLegal());
  }
}
