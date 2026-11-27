package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import tools.jackson.databind.annotation.JsonDeserialize;

/** Contratos HTTP de propuestas (SPEC-07). */
public final class PropuestaDtos {
    private PropuestaDtos() {}

    @Schema(name = "CrearPropuestaRequest")
    public record CrearRequest(@NotNull UUID solicitudId,
                               @NotBlank @Size(max = 255) String disponibilidad,
                               @Size(max = 1000) String mensaje,
                               @NotNull @Positive @Max(value = 2147483647, message = "La tarifa debe ser un entero positivo en pesos colombianos.")
                               @JsonDeserialize(using = TarifaCopDeserializer.class)
                               @Schema(description = "Tarifa acordada en pesos colombianos (COP). Entero positivo.", example = "250000")
                               Integer tarifaCop) {}

    @Schema(name = "EditarPropuestaRequest")
    public record EditarRequest(@Size(max = 255, message = "La disponibilidad admite máximo 255 caracteres") String disponibilidad,
                                @Size(max = 1000, message = "El mensaje admite máximo 1000 caracteres") String mensaje,
                                @Positive @Max(value = 2147483647, message = "La tarifa debe ser un entero positivo en pesos colombianos.")
                                @JsonDeserialize(using = TarifaCopDeserializer.class)
                                @Schema(description = "Tarifa acordada en pesos colombianos (COP). Si se omite, conserva la tarifa actual.", example = "250000")
                                Integer tarifaCop) {}

    public record PropuestaResponse(UUID id, UUID solicitudId, UUID prestadorId, String disponibilidad,
                                    String mensaje,
                                    @Schema(description = "Tarifa acordada en COP; null en propuestas históricas sin tarifa.")
                                    Integer tarifaCop,
                                    String estado) {}

    public record IdEstadoResponse(UUID id, String estado) {}
}
