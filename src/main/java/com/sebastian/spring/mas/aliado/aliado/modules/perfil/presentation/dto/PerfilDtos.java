package com.sebastian.spring.mas.aliado.aliado.modules.perfil.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Contratos HTTP de perfil y privacidad (SPEC-02). */
public final class PerfilDtos {
    private PerfilDtos() {}

    public record PerfilResponse(UUID usuarioId, String nombre, String telefono, String preferenciaVisibilidad,
                                 String zonaAproximada, String direccionExacta, int version) {}

    public record ActualizarPerfilRequest(@Size(max = 120, message = "El nombre admite máximo 120 caracteres") String nombre,
                                          @Pattern(regexp = "[+0-9 ()-]{7,30}", message = "Teléfono inválido") String telefono,
                                          Integer version) {}

    public record UbicacionRequest(@NotBlank @Size(max = 100, message = "La zona admite máximo 100 caracteres") String zonaAproximada,
                                   @Size(max = 500, message = "La dirección admite máximo 500 caracteres") String direccionExacta) {}

    public record VisibilidadRequest(@NotBlank String preferenciaVisibilidad) {}

    public record PerfilPublicoResponse(UUID usuarioId, String zonaAproximada) {}

    public record EliminacionResponse(String estado, OffsetDateTime expiraEn, long archivosPendientes,
                                      String politicaEliminacion) {}
}
