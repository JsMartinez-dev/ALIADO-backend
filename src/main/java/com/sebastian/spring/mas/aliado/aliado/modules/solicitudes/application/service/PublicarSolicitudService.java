package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.events.Eventos;
import com.sebastian.spring.mas.aliado.aliado.common.events.PublicadorEventos;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.DefinirZonaUrgenciaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.PublicarSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.DatosSolicitudInvalidosException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.SolicitudNoEncontradaException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.SolicitudNoModificableException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.ConsultaCategoriaSolicitudPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.DefinirZonaUrgenciaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.PublicarSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.repository.SolicitudRepository;
import java.time.OffsetDateTime;

public class PublicarSolicitudService implements PublicarSolicitudUseCase {
    private final SolicitudRepository solicitudesRepository;
    private final ConsultaCategoriaSolicitudPort consultaCategoriaSolicitudPort;
    private final DefinirZonaUrgenciaUseCase definirZonaUrgenciaUseCase;
    private final PublicadorEventos publicadorEventos;

    public PublicarSolicitudService(SolicitudRepository solicitudesRepository,
            ConsultaCategoriaSolicitudPort consultaCategoriaSolicitudPort,
            DefinirZonaUrgenciaUseCase definirZonaUrgenciaUseCase,
            PublicadorEventos publicadorEventos) {
        this.solicitudesRepository = solicitudesRepository;
        this.consultaCategoriaSolicitudPort = consultaCategoriaSolicitudPort;
        this.definirZonaUrgenciaUseCase = definirZonaUrgenciaUseCase;
        this.publicadorEventos = publicadorEventos;
    }

    @Override
    public SolicitudResult ejecutar(PublicarSolicitudCommand command) {
        Solicitud solicitudActual = solicitudesRepository.porId(command.solicitudId())
                .filter(solicitud -> solicitud.clienteId().equals(command.clienteId()))
                .orElseThrow(SolicitudNoEncontradaException::new);
        if (!solicitudActual.abierta()) {
            throw new SolicitudNoModificableException("SOLICITUD_CERRADA",
                    "La solicitud ya no admite cambios (estado " + solicitudActual.estado() + ").");
        }
        if (solicitudActual.estado() != EstadoSolicitud.BORRADOR) {
            throw new SolicitudNoModificableException("TRANSICION_INVALIDA", "Solo un borrador puede publicarse.");
        }
        if (solicitudActual.descripcion() == null || solicitudActual.descripcion().isBlank()
                || solicitudActual.categoriaId() == null || solicitudActual.zonaId() == null) {
            throw new DatosSolicitudInvalidosException("BORRADOR_INCOMPLETO", "Para publicar faltan la descripción, la categoría o la zona.");
        }
        if (!consultaCategoriaSolicitudPort.estaActiva(solicitudActual.categoriaId())) {
            throw new DatosSolicitudInvalidosException("CATEGORIA_INVALIDA", "La categoría no existe o no está activa.");
        }

        definirZonaUrgenciaUseCase.ejecutar(new DefinirZonaUrgenciaCommand(solicitudActual.zonaId(),
                solicitudActual.urgencia() == null ? null : solicitudActual.urgencia().name(), true));

        Solicitud solicitudPublicada = solicitudesRepository.guardar(
                solicitudActual.transitarA(EstadoSolicitud.PUBLICADA, OffsetDateTime.now()));

        publicadorEventos.publicar(new Eventos.SolicitudPublicada(
                solicitudPublicada.id(), command.clienteId(), solicitudPublicada.categoriaId(),
                solicitudPublicada.zonaId(), solicitudPublicada.urgencia().name(), OffsetDateTime.now()));

        return SolicitudResult.desde(solicitudPublicada);
    }
}
