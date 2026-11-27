package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Contratos HTTP de preferencias e historial (SPEC-14). */
public final class PreferenciasDtos {
    private PreferenciasDtos() {}

    public record PreferenciaResponse(String tipoAviso, String canal, boolean habilitada, boolean configurable) {}
    public record ActualizarPreferenciaRequest(@NotBlank String tipoAviso, @NotBlank String canal,
                                               boolean habilitada) {}
    public record ServicioResumen(UUID id, String estado, OffsetDateTime fechaContratacion) {}
    public record DescargaResponse(UUID id, String archivoKey, String estado) {}
    public record IndicadoresResponse(long totalServicios, long enCurso, long finalizados,
                                      double reputacion, long cantidadCalificaciones) {}
    public record ActividadResponse(String tipo, String resumen, OffsetDateTime fecha,
                                    String referenciaTipo, UUID referenciaId) {}
}
