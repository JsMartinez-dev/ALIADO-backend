package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PerfilProfesionalDtos {
    private PerfilProfesionalDtos() {}

    public record ExperienciaInput(String descripcion, LocalDate fechaInicio, LocalDate fechaFin) {}

    @Schema(name = "CrearPerfilProfesionalRequest")
    public record CrearRequest(Map<String, Object> datosGenerales,
                               @NotNull List<UUID> categoriaIds,
                               @NotNull List<UUID> zonaIds,
                               List<ExperienciaInput> experiencia) {}

    public record ActualizarDatosRequest(Map<String, Object> datosGenerales) {}

    public record IdsRequest(@NotNull List<UUID> ids) {}

    public record ExperienciaRequest(@NotBlank String descripcion, @NotNull LocalDate fechaInicio, LocalDate fechaFin) {}

    public record BloqueDisponibilidad(Integer diaSemana, @NotNull LocalTime horaInicio, @NotNull LocalTime horaFin) {}

    public record DisponibilidadRequest(@NotNull List<BloqueDisponibilidad> bloques) {}

    public record CompromisoRequest(UUID id, @NotNull OffsetDateTime fechaHoraInicio, @NotNull OffsetDateTime fechaHoraFin) {}

    public record PerfilResponse(UUID id, UUID prestadorId, Map<String, Object> datosGenerales,
                                 String estadoExposicion, List<UUID> categoriaIds, List<UUID> zonaIds,
                                 List<ExperienciaResponse> experiencias, List<PortafolioResponse> portafolio,
                                 List<DisponibilidadResponse> disponibilidades, List<CompromisoResponse> compromisos) {}

    public record ExperienciaResponse(UUID id, String descripcion, LocalDate fechaInicio, LocalDate fechaFin) {}

    public record PortafolioResponse(UUID id, String descripcion, String evidenciaKey, OffsetDateTime fechaCreacion) {}

    public record DisponibilidadResponse(UUID id, Integer diaSemana, LocalTime horaInicio, LocalTime horaFin) {}

    public record CompromisoResponse(UUID id, OffsetDateTime fechaHoraInicio, OffsetDateTime fechaHoraFin, String origen) {}

    public record IdResponse(UUID id) {}

    public record ExposicionRequest(String estadoExposicion) {}

    public record ResenaPublicaResponse(UUID id, int valor, String texto, OffsetDateTime fechaCreacion) {}

    /** Vista previa = vista pública: sin compromisos, portafolio ni experiencias. */
    public record VistaPreviaResponse(UUID prestadorId, List<String> categorias, List<String> zonas,
                                      java.math.BigDecimal reputacion, int cantidadCalificaciones,
                                      String nivelVerificacion, List<DisponibilidadResponse> disponibilidad,
                                      Map<String, Object> datosGenerales,
                                      List<ResenaPublicaResponse> resenas) {}
}
