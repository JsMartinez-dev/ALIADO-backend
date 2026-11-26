package com.sebastian.spring.mas.aliado.aliado.modules.confianza.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class ConfianzaDtos {
    private ConfianzaDtos() {}

    public record CalificarRequest(@Min(1) @Max(5) int valor) {}

    public record CalificacionResponse(UUID id, BigDecimal reputacionActualizada) {}

    public record ResenaRequest(@NotBlank String texto) {}

    public record ResenaResponse(UUID id) {}

    public record ConfianzaResponse(BigDecimal reputacion, int cantidadCalificaciones, String nivelVerificacion) {}

    public record ResenaPublicaResponse(UUID id, int valor, String texto, OffsetDateTime fechaCreacion) {}

    public record VerificacionResponse(UUID id, String estado) {}

    public record ReporteRequest(@NotBlank String motivo) {}

    public record ReporteResponse(UUID reporteId) {}
}
