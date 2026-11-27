package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.presentation.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Contratos HTTP del tablero de oportunidades (SPEC-06). */
public final class OportunidadDtos {
    private OportunidadDtos() {}

    public record OportunidadResponse(UUID solicitudId, String categoria, String zonaAproximada, String urgencia,
                                      String descripcion, OffsetDateTime fechaCreacion,
                                      List<FotoDetalleResponse> fotosDetalle) {}

    public record FotoDetalleResponse(UUID id, int orden) {}
}
