package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/** Contratos HTTP de formalización (SPEC-13). */
public final class FormalizacionDtos {
  private FormalizacionDtos() {}

  public record EnlaceResponse(UUID id, String nombre, String url, String estado) {}

  public record PasoResponse(
      UUID id, String titulo, String descripcion, int orden, List<EnlaceResponse> enlaces) {}

  public record RutaResponse(
      UUID id, String nombre, String descripcion, List<PasoResponse> pasos, String avisoLegal) {}

  public record ProgresoResponse(UUID rutaId, int valorOrientativo, String avisoLegal) {}

  public record ActualizarProgresoRequest(@NotNull UUID rutaId, @Min(0) @Max(100) int valor) {}
}
