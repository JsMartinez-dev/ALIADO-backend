package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service;

import java.util.UUID;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.SolicitudNoEncontradaException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.SolicitudNoModificableException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.repository.SolicitudRepository;
/** Comprobaciones de propiedad y estado compartidas por las operaciones del agregado. */
public final class AccesoSolicitud {
    private AccesoSolicitud() {}
    public static Solicitud propiaAbierta(SolicitudRepository solicitudes, UUID id, UUID usuarioId) {
        Solicitud actual = solicitudes.porId(id).filter(s -> s.clienteId().equals(usuarioId))
                .orElseThrow(SolicitudNoEncontradaException::new);
        if (!actual.abierta()) {
            throw new SolicitudNoModificableException("SOLICITUD_CERRADA",
                    "La solicitud ya no admite cambios (estado " + actual.estado() + ").");
        }
        return actual;
    }
}
