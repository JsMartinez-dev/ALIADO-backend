package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.presentation.mapper;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.CrearSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.PublicarSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.presentation.dto.SolicitudDtos;
import java.util.UUID;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.ConsultarSolicitudPropiaQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.CancelarSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.EditarSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.GuardarFotoSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.ConsultarFotoSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.FotoSolicitudResult;

public final class SolicitudPresentationMapper {
    private SolicitudPresentationMapper() {}

    public static GuardarFotoSolicitudCommand aFotoCommand(UUID id, UUID usuarioId, UUID fotoId,
            String nombre, String tipo, byte[] contenido) {
        return new GuardarFotoSolicitudCommand(id, usuarioId, fotoId, nombre, tipo, contenido);
    }

    public static ConsultarFotoSolicitudCommand aConsultarFotoCommand(UUID id, UUID usuarioId, UUID fotoId) {
        return new ConsultarFotoSolicitudCommand(id, usuarioId, fotoId);
    }

    public static SolicitudDtos.FotoResponse aResponse(FotoSolicitudResult foto) {
        return new SolicitudDtos.FotoResponse(foto.id(), foto.archivoKey(), foto.orden());
    }

    public static EditarSolicitudCommand aCommand(UUID id, UUID clienteId, SolicitudDtos.EditarRequest req) {
        return new EditarSolicitudCommand(id, clienteId, req.categoriaId(), req.descripcion(), req.zonaId(), req.urgencia());
    }

    public static CancelarSolicitudCommand aCancelarCommand(UUID id, UUID clienteId) {
        return new CancelarSolicitudCommand(id, clienteId);
    }

    public static SolicitudDtos.IdEstadoResponse aEstadoResponse(SolicitudResult result) {
        return new SolicitudDtos.IdEstadoResponse(result.id(), result.estado());
    }

    public static ConsultarSolicitudPropiaQuery aConsultaPropia(UUID id, UUID clienteId) {
        return new ConsultarSolicitudPropiaQuery(id, clienteId);
    }

    public static CrearSolicitudCommand aCommand(UUID clienteId, SolicitudDtos.CrearRequest req) {
        return new CrearSolicitudCommand(clienteId, req.categoriaId(), req.descripcion(), req.zonaId(), req.urgencia(), req.estadoDeseado());
    }

    public static PublicarSolicitudCommand aPublicarCommand(UUID solicitudId, UUID clienteId) {
        return new PublicarSolicitudCommand(solicitudId, clienteId);
    }

    public static SolicitudDtos.SolicitudResponse aResponse(SolicitudResult result) {
        return new SolicitudDtos.SolicitudResponse(result.id(), result.clienteId(), result.categoriaId(), result.zonaId(),
                result.descripcion(), result.urgencia(), result.estado(), result.fechaCreacion(), result.fechaActualizacion(), result.fotos());
    }
}
