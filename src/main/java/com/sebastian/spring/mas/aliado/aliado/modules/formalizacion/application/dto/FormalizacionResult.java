package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto;

import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.FormalizacionService;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.domain.model.*;
import java.util.*;

public final class FormalizacionResult {
  private FormalizacionResult() {}

  public record EnlaceResult(UUID id, String nombre, String url, String estado) {}

  public record PasoResult(
      UUID id, String titulo, String descripcion, int orden, List<EnlaceResult> enlaces) {}

  public record RutaResult(
      UUID id, String nombre, String descripcion, List<PasoResult> pasos, String avisoLegal) {}

  public record ProgresoResult(UUID rutaId, int valorOrientativo, String avisoLegal) {}

  public static EnlaceResult enlace(Enlace e) {
    return new EnlaceResult(e.id(), e.nombre(), e.url(), e.estado().name());
  }

  public static ProgresoResult progreso(Progreso p) {
    return new ProgresoResult(p.rutaId(), p.valorOrientativo(), FormalizacionService.AVISO_LEGAL);
  }
}
