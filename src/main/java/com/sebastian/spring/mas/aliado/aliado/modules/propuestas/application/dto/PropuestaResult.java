package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.dto;
import java.util.UUID;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model.Propuesta;
public record PropuestaResult(UUID id, UUID solicitudId, UUID prestadorId, String disponibilidad, String mensaje, Integer tarifaCop, String estado) {
    public static PropuestaResult desde(Propuesta p) {
        return new PropuestaResult(p.id(), p.solicitudId(), p.prestadorId(), p.disponibilidad(), p.mensaje(), p.tarifaCop(), p.estado().name());
    }
}
