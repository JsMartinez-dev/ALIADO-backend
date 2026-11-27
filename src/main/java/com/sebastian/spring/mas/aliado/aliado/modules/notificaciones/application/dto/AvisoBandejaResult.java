package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Aviso de una página de la bandeja (F09-BANDEJA-03). Conserva los campos del contrato
 * vigente y agrega {@code leidaEn}, que es {@code null} mientras el aviso esté pendiente.
 */
public record AvisoBandejaResult(UUID id, String tipo, UUID servicioId, UUID solicitudId, UUID propuestaId,
                                 String detalle, String estadoEntrega,
                                 OffsetDateTime fechaGeneracion, OffsetDateTime leidaEn) {

    /** Reutiliza la interpretación del agregado que ya usa la consulta de la bandeja. */
    public static AvisoBandejaResult desde(Notificacion notificacion, UUID solicitudId) {
        return new AvisoBandejaResult(notificacion.id(), notificacion.tipo().name(),
                notificacion.servicioId(), solicitudId,
                solicitudId == null ? null : notificacion.propuestaId(), notificacion.detalle(),
                notificacion.estadoEntrega().name(), notificacion.fechaGeneracion(),
                notificacion.leidaEn());
    }
}
