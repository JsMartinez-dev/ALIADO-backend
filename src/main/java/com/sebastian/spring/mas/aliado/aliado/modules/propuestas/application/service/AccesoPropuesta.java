package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.repository.PropuestaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.exception.PropuestaNoActivaException;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.exception.RecursoPropuestaNoEncontradoException;
import java.util.UUID;
final class AccesoPropuesta {
    private AccesoPropuesta() {}
    static Propuesta propiaActiva(PropuestaRepository propuestas, UUID id, UUID prestadorId) {
        Propuesta actual = propuestas.porId(id).filter(p -> p.prestadorId().equals(prestadorId))
                .orElseThrow(() -> new RecursoPropuestaNoEncontradoException("PROPUESTA_NO_ENCONTRADA", "Propuesta no encontrada."));
        if (actual.estado() != EstadoPropuesta.ACTIVA) { throw new PropuestaNoActivaException(actual.estado().name()); }
        return actual;
    }
}
