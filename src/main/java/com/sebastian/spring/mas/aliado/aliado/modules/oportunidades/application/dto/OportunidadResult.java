package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
public record OportunidadResult(UUID solicitudId, String categoria, String zonaAproximada, String urgencia,
                                String descripcion, OffsetDateTime fechaCreacion,
                                List<FotoDetalleResult> fotosDetalle, String nombreVisibleCliente) {
    public record FotoDetalleResult(UUID id, int orden) {}
}
