package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/** Contratos HTTP de propuestas (SPEC-07). */
public final class PropuestaDtos {
    private PropuestaDtos() {}

    @Schema(name = "CrearPropuestaRequest")
    public record CrearRequest(@NotNull UUID solicitudId, @NotBlank @Size(max = 255) String disponibilidad, @Size(max = 1000) String mensaje) {}

    @Schema(name = "EditarPropuestaRequest")
    public record EditarRequest(@Size(max = 255, message = "La disponibilidad admite máximo 255 caracteres") String disponibilidad,
                                @Size(max = 1000, message = "El mensaje admite máximo 1000 caracteres") String mensaje) {}

    public record PropuestaResponse(UUID id, UUID solicitudId, UUID prestadorId, String disponibilidad,
                                    String mensaje, String estado) {}

    public record IdEstadoResponse(UUID id, String estado) {}
}
