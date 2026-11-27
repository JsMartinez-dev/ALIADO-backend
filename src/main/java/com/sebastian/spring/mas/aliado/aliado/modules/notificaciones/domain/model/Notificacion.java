package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Notificación unificada (plan general 6.2 y 19): un solo modelo para SPEC-06/07/09. */
public record Notificacion(UUID id, UUID destinatarioId, TipoNotificacion tipo, UUID aggregateId,
                           int eventVersion, String detalle, Canal canal, EstadoEntrega estadoEntrega,
                           int intentos, OffsetDateTime fechaGeneracion, OffsetDateTime fechaEntrega,
                           OffsetDateTime leidaEn) {

    /**
     * Servicio al que se refiere el aviso cuando el agregado de origen es un servicio
     * contratado (SPEC-07/SPEC-08). La interpretación del agregado es una regla del
     * dominio y la comparten la bandeja y las consultas existentes.
     */
    public UUID servicioId() {
        return tipo == TipoNotificacion.ACEPTACION_PROPUESTA
                || tipo == TipoNotificacion.CAMBIO_ESTADO_SERVICIO ? aggregateId : null;
    }

    public UUID propuestaId() {
        return tipo == TipoNotificacion.PROPUESTA_RECIBIDA ? aggregateId : null;
    }

    /** Solicitud que originó el aviso; el agregado de SPEC-06 es la propia solicitud. */
    public UUID solicitudId() {
        return tipo == TipoNotificacion.NUEVA_OPORTUNIDAD ? aggregateId : null;
    }
}
