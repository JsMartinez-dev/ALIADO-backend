package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public record Solicitud(UUID id, UUID clienteId, UUID categoriaId, UUID zonaId, String descripcion,
                        Urgencia urgencia, EstadoSolicitud estado, OffsetDateTime fechaCreacion,
                        OffsetDateTime fechaActualizacion, int version,
                        List<FotoSolicitud> fotos, List<HistorialEstadoSolicitud> historial) {

    public boolean abierta() {
        return estado == EstadoSolicitud.BORRADOR || estado == EstadoSolicitud.PUBLICADA;
    }

    public Solicitud transitarA(EstadoSolicitud nuevo, OffsetDateTime ahora) {
        boolean permitido = switch (estado) {
            case BORRADOR -> nuevo == EstadoSolicitud.PUBLICADA || nuevo == EstadoSolicitud.CANCELADA;
            case PUBLICADA -> nuevo == EstadoSolicitud.CANCELADA || nuevo == EstadoSolicitud.CONTRATADA;
            case CANCELADA, CONTRATADA -> false;
        };
        if (!permitido) {
            throw new BusinessException.Conflict("TRANSICION_INVALIDA",
                    "No se puede pasar de " + estado + " a " + nuevo + ".");
        }
        var nuevoHistorial = new ArrayList<>(historial);
        nuevoHistorial.add(new HistorialEstadoSolicitud(UUID.randomUUID(), id, estado, nuevo, ahora));
        return new Solicitud(id, clienteId, categoriaId, zonaId, descripcion, urgencia, nuevo,
                fechaCreacion, ahora, version, fotos, List.copyOf(nuevoHistorial));
    }

    public Solicitud conFoto(FotoSolicitud foto) {
        var nuevas = new ArrayList<>(fotos);
        nuevas.add(foto);
        return new Solicitud(id, clienteId, categoriaId, zonaId, descripcion, urgencia, estado,
                fechaCreacion, fechaActualizacion, version, List.copyOf(nuevas), historial);
    }

    public Solicitud conFotoReemplazada(FotoSolicitud foto) {
        var nuevas = fotos.stream()
                .map(f -> f.id().equals(foto.id()) ? foto : f)
                .toList();
        return new Solicitud(id, clienteId, categoriaId, zonaId, descripcion, urgencia, estado,
                fechaCreacion, fechaActualizacion, version, nuevas, historial);
    }

    public Solicitud sinFoto(UUID fotoId) {
        var nuevas = fotos.stream().filter(f -> !f.id().equals(fotoId)).toList();
        return new Solicitud(id, clienteId, categoriaId, zonaId, descripcion, urgencia, estado,
                fechaCreacion, fechaActualizacion, version, nuevas, historial);
    }
}
