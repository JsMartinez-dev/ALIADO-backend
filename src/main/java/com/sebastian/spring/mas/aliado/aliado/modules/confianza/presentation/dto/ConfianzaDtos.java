package com.sebastian.spring.mas.aliado.aliado.modules.confianza.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Contratos HTTP de confianza (SPEC-10). */
public final class ConfianzaDtos {
    private ConfianzaDtos() {}

    public record CalificarRequest(@Min(1) @Max(5) int valor) {}

    public record CalificacionResponse(UUID id, BigDecimal reputacionActualizada) {}

    public record CalificacionPropiaResponse(UUID calificacionId, int valor, boolean tieneResena) {}

    public record CalificacionHistorialItem(UUID calificacionId, UUID servicioId, int valor,
                                             OffsetDateTime fechaCalificacion, ResenaHistorial resena) {}
    public record ResenaHistorial(UUID id, String texto, OffsetDateTime fechaCreacion) {}
    public record CalificacionesPropiasPage(List<CalificacionHistorialItem> items, int pagina, int tamano,
                                            long totalElementos, int totalPaginas) {}

    public record ResenaRequest(@NotBlank String texto) {}

    public record ResenaResponse(UUID id) {}

    public record ConfianzaResponse(BigDecimal reputacion, int cantidadCalificaciones, String nivelVerificacion) {}

    public record ResenaPublicaResponse(UUID id, int valor, String texto, OffsetDateTime fechaCreacion) {}

    public record VerificacionResponse(UUID id, String estado) {}

    public record ReporteRequest(@NotBlank String motivo) {}

    public record ReporteResponse(UUID reporteId) {}
}
