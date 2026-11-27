package com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Contratos HTTP de descubrimiento de prestadores (SPEC-03). */
public final class DescubrimientoDtos {
    private DescubrimientoDtos() {}

    public record PrestadorPublicoResponse(UUID id, String nombreVisible, List<String> categorias, List<String> zonas,
                                           BigDecimal reputacionAgregada, int cantidadCalificaciones,
                                           String nivelVerificacion, List<BloqueResponse> disponibilidad,
                                           Map<String, Object> datosGenerales,
                                           List<ResenaPublicaResponse> resenas) {}

    public record BloqueResponse(Integer diaSemana, LocalTime horaInicio, LocalTime horaFin) {}

    public record ResenaPublicaResponse(UUID id, int valor, String texto, OffsetDateTime fechaCreacion) {}

    public record BusquedaResponse(List<PrestadorPublicoResponse> prestadores, List<String> sugerencias) {}
}
