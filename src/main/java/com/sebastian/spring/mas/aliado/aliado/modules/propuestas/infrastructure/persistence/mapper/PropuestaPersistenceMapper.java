package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.infrastructure.persistence.mapper;

import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model.EstadoPropuesta;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model.Propuesta;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.infrastructure.persistence.entity.PropuestaEntity;

public final class PropuestaPersistenceMapper {
    private PropuestaPersistenceMapper() {}

    public static Propuesta aDominio(PropuestaEntity e) {
        return new Propuesta(e.getId(), e.getSolicitudId(), e.getPrestadorId(), e.getDisponibilidad(),
                e.getMensaje(), e.getTarifaCop(), EstadoPropuesta.valueOf(e.getEstado()), e.getFechaCreacion(),
                e.getFechaActualizacion(), e.getVersion());
    }

    public static PropuestaEntity aEntidad(Propuesta p) {
        PropuestaEntity e = new PropuestaEntity();
        e.setId(p.id());
        e.setSolicitudId(p.solicitudId());
        e.setPrestadorId(p.prestadorId());
        e.setDisponibilidad(p.disponibilidad());
        e.setMensaje(p.mensaje());
        e.setTarifaCop(p.tarifaCop());
        e.setEstado(p.estado().name());
        e.setFechaCreacion(p.fechaCreacion());
        e.setFechaActualizacion(p.fechaActualizacion());
        e.setVersion(p.version());
        return e;
    }
}
