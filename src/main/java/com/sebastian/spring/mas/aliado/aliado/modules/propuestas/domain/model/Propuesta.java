package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model;

import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.exception.PropuestaNoActivaException;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Propuesta de un Prestador sobre una solicitud (SPEC-07), con tarifa acordada opcional para históricos. */
public record Propuesta(UUID id, UUID solicitudId, UUID prestadorId, String disponibilidad, String mensaje,
                        Integer tarifaCop, EstadoPropuesta estado, OffsetDateTime fechaCreacion,
                        OffsetDateTime fechaActualizacion, int version) {

    public Propuesta(UUID id, UUID solicitudId, UUID prestadorId, String disponibilidad, String mensaje,
                     EstadoPropuesta estado, OffsetDateTime fechaCreacion, OffsetDateTime fechaActualizacion,
                     int version) {
        this(id, solicitudId, prestadorId, disponibilidad, mensaje, null, estado, fechaCreacion,
                fechaActualizacion, version);
    }

    public Propuesta editada(String disponibilidad, String mensaje, OffsetDateTime ahora) {
        return editada(disponibilidad, mensaje, null, ahora);
    }

    public Propuesta editada(String disponibilidad, String mensaje, Integer tarifaCop, OffsetDateTime ahora) {
        exigirActiva();
        return new Propuesta(id, solicitudId, prestadorId,
                disponibilidad == null ? this.disponibilidad : disponibilidad.trim(),
                mensaje == null ? this.mensaje : mensaje,
                tarifaCop == null ? this.tarifaCop : tarifaCop,
                EstadoPropuesta.ACTIVA, fechaCreacion, ahora, version);
    }

    public Propuesta retirar(OffsetDateTime ahora) {
        exigirActiva();
        return new Propuesta(id, solicitudId, prestadorId, disponibilidad, mensaje, tarifaCop,
                EstadoPropuesta.RETIRADA, fechaCreacion, ahora, version);
    }

    public Propuesta aceptar(OffsetDateTime ahora) {
        exigirActiva();
        return new Propuesta(id, solicitudId, prestadorId, disponibilidad, mensaje, tarifaCop,
                EstadoPropuesta.ACEPTADA, fechaCreacion, ahora, version);
    }

    public Propuesta cerrar(OffsetDateTime ahora) {
        exigirActiva();
        return new Propuesta(id, solicitudId, prestadorId, disponibilidad, mensaje, tarifaCop,
                EstadoPropuesta.CERRADA, fechaCreacion, ahora, version);
    }

    private void exigirActiva() {
        if (estado != EstadoPropuesta.ACTIVA) {
            throw new PropuestaNoActivaException(estado.name());
        }
    }
}
