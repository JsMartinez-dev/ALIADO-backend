package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.domain.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Oportunidad = solicitud publicada vista desde un Prestador compatible (SPEC-06, proyección). */
public record Oportunidad(UUID solicitudId, String categoria, String zonaAproximada, String urgencia,
                          String descripcion, OffsetDateTime fechaCreacion,
                          List<FotoDetalle> fotosDetalle) {
    public record FotoDetalle(UUID id, int orden) {}
}
