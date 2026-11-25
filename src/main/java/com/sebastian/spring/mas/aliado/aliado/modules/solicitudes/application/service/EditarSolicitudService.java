package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service;

import java.time.OffsetDateTime;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.EditarSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.DatosSolicitudInvalidosException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.ConsultaCategoriaSolicitudPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.ConsultaZonaSolicitudPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.EditarSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Urgencia;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.repository.SolicitudRepository;
public class EditarSolicitudService implements EditarSolicitudUseCase {
    private final SolicitudRepository solicitudesRepository;
    private final ConsultaCategoriaSolicitudPort consultaCategoriaSolicitudPort;
    private final ConsultaZonaSolicitudPort consultaZonaSolicitudPort;

    public EditarSolicitudService(SolicitudRepository solicitudesRepository,
            ConsultaCategoriaSolicitudPort consultaCategoriaSolicitudPort,
            ConsultaZonaSolicitudPort consultaZonaSolicitudPort) {
        this.solicitudesRepository = solicitudesRepository;
        this.consultaCategoriaSolicitudPort = consultaCategoriaSolicitudPort;
        this.consultaZonaSolicitudPort = consultaZonaSolicitudPort;
    }

    @Override
    public SolicitudResult ejecutar(EditarSolicitudCommand command) {
        Solicitud solicitudActual = AccesoSolicitud.propiaAbierta(
                solicitudesRepository, command.solicitudId(), command.clienteId());
        var categoriaId = solicitudActual.categoriaId();
        var zonaId = solicitudActual.zonaId();
        Urgencia urgencia = solicitudActual.urgencia();
        if (command.categoriaId() != null) {
            if (!consultaCategoriaSolicitudPort.estaActiva(command.categoriaId())) {
                throw new DatosSolicitudInvalidosException("CATEGORIA_INVALIDA", "La categoría no existe o no está activa.");
            }
            categoriaId = command.categoriaId();
        }
        if (command.zonaId() != null) {
            if (!consultaZonaSolicitudPort.estaActiva(command.zonaId())) {
                throw new DatosSolicitudInvalidosException("ZONA_INVALIDA", "La zona no existe o no está activa.");
            }
            zonaId = command.zonaId();
        }
        if (command.urgencia() != null) {
            try {
                urgencia = Urgencia.valueOf(command.urgencia());
            }
            catch (IllegalArgumentException ex) {
                throw new DatosSolicitudInvalidosException("URGENCIA_INVALIDA", "Urgencia inválida.");
            }
        }
        String descripcionSolicitud =
                command.descripcion() == null ? solicitudActual.descripcion() : command.descripcion().trim();
        if (solicitudActual.estado() == EstadoSolicitud.PUBLICADA
                && (descripcionSolicitud == null || descripcionSolicitud.isBlank())) {
            throw new DatosSolicitudInvalidosException("DESCRIPCION_REQUERIDA", "La descripción es obligatoria.");
        }
        Solicitud solicitudEditada = new Solicitud(solicitudActual.id(), command.clienteId(), categoriaId, zonaId,
                descripcionSolicitud, urgencia, solicitudActual.estado(), solicitudActual.fechaCreacion(),
                OffsetDateTime.now(), solicitudActual.version(), solicitudActual.fotos(),
                solicitudActual.historial());
        return SolicitudResult.desde(solicitudesRepository.guardar(solicitudEditada));
    }
}
