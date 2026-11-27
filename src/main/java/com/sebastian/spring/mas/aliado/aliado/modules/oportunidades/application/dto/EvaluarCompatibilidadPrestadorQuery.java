package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto;

import java.util.Objects;
import java.util.UUID;

/** Consulta de compatibilidad de una solicitud para un prestador concreto (F09-PUSH-08). */
public record EvaluarCompatibilidadPrestadorQuery(UUID solicitudId, UUID prestadorId) {

    public EvaluarCompatibilidadPrestadorQuery {
        Objects.requireNonNull(solicitudId, "La solicitud es obligatoria");
        Objects.requireNonNull(prestadorId, "El prestador es obligatorio");
    }
}
