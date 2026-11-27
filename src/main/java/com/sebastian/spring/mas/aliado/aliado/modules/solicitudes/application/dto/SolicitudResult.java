package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SolicitudResult(UUID id, UUID clienteId, UUID categoriaId, UUID zonaId, String descripcion,
                              String urgencia, String estado, OffsetDateTime fechaCreacion,
                              OffsetDateTime fechaActualizacion, List<String> fotos, List<FotoDetalleResult> fotosDetalle) {
    public record FotoDetalleResult(UUID id, String archivoKey, int orden) {}

    public static SolicitudResult desde(Solicitud s) {
        return new SolicitudResult(s.id(), s.clienteId(), s.categoriaId(), s.zonaId(), s.descripcion(),
                s.urgencia() == null ? null : s.urgencia().name(), s.estado().name(),
                s.fechaCreacion(), s.fechaActualizacion(), s.fotos().stream().map(f -> f.archivoKey()).toList(),
                s.fotos().stream().map(f -> new FotoDetalleResult(f.id(), f.archivoKey(), f.orden())).toList());
    }
}
