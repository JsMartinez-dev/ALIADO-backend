package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.presentation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.DatosGeneralesProfesional;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.Map;

/** Contrato HTTP mínimo de datos generales del perfil profesional (BE-05). */
public final class DatosGeneralesDtos {
    private DatosGeneralesDtos() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DatosGeneralesRequest(
            @Size(max = DatosGeneralesProfesional.MAX_DESCRIPCION,
                    message = "descripcion admite máximo 1000 caracteres")
            @Schema(description = "Presentación libre del Prestador. No incluye categorías, zonas, "
                    + "experiencia, portafolio, disponibilidad ni agenda.",
                    example = "Plomero con experiencia en reparaciones domiciliarias.")
            String descripcion) {}

    public record DatosGeneralesResponse(
            @Schema(description = "Presentación libre del Prestador.",
                    example = "Plomero con experiencia en reparaciones domiciliarias.")
            String descripcion) {
        public static DatosGeneralesResponse desde(Map<String, Object> datos) {
            return new DatosGeneralesResponse(DatosGeneralesProfesional.descripcionDe(datos));
        }
    }
}
