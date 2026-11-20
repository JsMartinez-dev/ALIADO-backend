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
    private final SolicitudRepository solicitudes;
    private final ConsultaCategoriaSolicitudPort categorias;
    private final DefinirZonaUrgenciaUseCase definirZonaUrgencia;
    private final PublicadorEventos eventos;

    public PublicarSolicitudService(SolicitudRepository solicitudes, ConsultaCategoriaSolicitudPort categorias,
                                    DefinirZonaUrgenciaUseCase definirZonaUrgencia, PublicadorEventos eventos) {
        this.solicitudes = solicitudes;
        this.categorias = categorias;
        this.definirZonaUrgencia = definirZonaUrgencia;
        this.eventos = eventos;
    }

    @Override
    public SolicitudResult ejecutar(PublicarSolicitudCommand command) {
        Solicitud actual = solicitudes.porId(command.solicitudId()).filter(s -> s.clienteId().equals(command.clienteId()))
                .orElseThrow(SolicitudNoEncontradaException::new);
        if (!actual.abierta()) {
            throw new SolicitudNoModificableException("SOLICITUD_CERRADA",
                    "La solicitud ya no admite cambios (estado " + actual.estado() + ").");
        }
        if (actual.estado() != EstadoSolicitud.BORRADOR) {
            throw new SolicitudNoModificableException("TRANSICION_INVALIDA", "Solo un borrador puede publicarse.");
        }
        if (actual.descripcion() == null || actual.descripcion().isBlank() || actual.categoriaId() == null || actual.zonaId() == null) {
            throw new DatosSolicitudInvalidosException("BORRADOR_INCOMPLETO", "Para publicar faltan la descripción, la categoría o la zona.");
        }
        if (!categorias.estaActiva(actual.categoriaId())) {
            throw new DatosSolicitudInvalidosException("CATEGORIA_INVALIDA", "La categoría no existe o no está activa.");
        }
        definirZonaUrgencia.ejecutar(new DefinirZonaUrgenciaCommand(actual.zonaId(),
                actual.urgencia() == null ? null : actual.urgencia().name(), true));
        Solicitud publicada = solicitudes.guardar(actual.transitarA(EstadoSolicitud.PUBLICADA, OffsetDateTime.now()));
        eventos.publicar(new Eventos.SolicitudPublicada(publicada.id(), command.clienteId(), publicada.categoriaId(),
                publicada.zonaId(), publicada.urgencia().name(), OffsetDateTime.now()));
        return SolicitudResult.desde(publicada);
    }
}
