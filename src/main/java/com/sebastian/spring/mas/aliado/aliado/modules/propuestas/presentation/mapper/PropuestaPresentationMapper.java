package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.presentation.mapper;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.presentation.dto.PropuestaDtos;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.dto.*;
import java.util.UUID;
public final class PropuestaPresentationMapper {
    private PropuestaPresentationMapper() {}
    public static CrearPropuestaCommand aCommand(UUID prestadorId, PropuestaDtos.CrearRequest req) {
        return new CrearPropuestaCommand(prestadorId, req.solicitudId(), req.disponibilidad(), req.mensaje());
    }
    public static PropuestaDtos.PropuestaResponse aResponse(PropuestaResult result) {
        return new PropuestaDtos.PropuestaResponse(result.id(), result.solicitudId(), result.prestadorId(), result.disponibilidad(), result.mensaje(), result.estado());
    }

    public static EditarPropuestaCommand aEditarCommand(UUID id, UUID prestadorId, PropuestaDtos.EditarRequest req) {
        return new EditarPropuestaCommand(id, prestadorId, req.disponibilidad(), req.mensaje());
    }
    public static RetirarPropuestaCommand aRetirarCommand(UUID id, UUID prestadorId) {
        return new RetirarPropuestaCommand(id, prestadorId);
    }
    public static PropuestaDtos.IdEstadoResponse aEstadoResponse(PropuestaResult result) {
        return new PropuestaDtos.IdEstadoResponse(result.id(), result.estado());
    }
}
