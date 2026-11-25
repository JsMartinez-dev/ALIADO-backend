package com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model;

import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.exception.TransicionServicioInvalidaException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public record ServicioContratado(UUID id, UUID solicitudId, UUID propuestaId, UUID clienteId, UUID prestadorId,
                                 EstadoServicio estado, boolean comunicacionHabilitada,
                                 OffsetDateTime fechaContratacion, int version,
                                 List<HistorialEstadoServicio> historial, List<MensajeServicio> mensajes) {

    public boolean esParticipante(UUID usuarioId) {
        return clienteId.equals(usuarioId) || prestadorId.equals(usuarioId);
    }

    public boolean activo() {
        return estado == EstadoServicio.CONTRATADO || estado == EstadoServicio.EN_EJECUCION
                || estado == EstadoServicio.TERMINADO;
    }

    public ServicioContratado conMensaje(MensajeServicio mensaje) {
        var mensajes = new ArrayList<>(this.mensajes);
        mensajes.add(mensaje);
        return new ServicioContratado(id, solicitudId, propuestaId, clienteId, prestadorId, estado,
                comunicacionHabilitada, fechaContratacion, version, historial, List.copyOf(mensajes));
    }

    public ServicioContratado transitarA(EstadoServicio nuevo, UUID actorId, OffsetDateTime ahora) {
        boolean permitido = switch (estado) {
            case CONTRATADO -> nuevo == EstadoServicio.EN_EJECUCION || nuevo == EstadoServicio.CANCELADO;
            case EN_EJECUCION -> nuevo == EstadoServicio.TERMINADO || nuevo == EstadoServicio.CANCELADO;
            case TERMINADO -> nuevo == EstadoServicio.FINALIZADO || nuevo == EstadoServicio.CANCELADO;
            case FINALIZADO, CANCELADO -> false;
        };
        if (!permitido) {
            throw new TransicionServicioInvalidaException(
                    "No se puede pasar de " + estado + " a " + nuevo + ".");
        }
        var nuevoHistorial = new ArrayList<>(historial);
        nuevoHistorial.add(new HistorialEstadoServicio(UUID.randomUUID(), id, estado, nuevo, actorId, ahora));
        return new ServicioContratado(id, solicitudId, propuestaId, clienteId, prestadorId, nuevo,
                comunicacionHabilitada, fechaContratacion, version, List.copyOf(nuevoHistorial), mensajes);
    }
}
