package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.events.Eventos;
import com.sebastian.spring.mas.aliado.aliado.common.events.PublicadorEventos;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.CrearSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.DefinirZonaUrgenciaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.DatosSolicitudInvalidosException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.ConsultaCategoriaSolicitudPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.DefinirZonaUrgenciaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.HistorialEstadoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.repository.SolicitudRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Construcción compartida por publicación inmediata y guardado de borrador. */
public class CreacionSolicitud {
    private final SolicitudRepository solicitudes;
    private final ConsultaCategoriaSolicitudPort categorias;
    private final DefinirZonaUrgenciaUseCase definirZonaUrgencia;
    private final PublicadorEventos eventos;

    public CreacionSolicitud(SolicitudRepository solicitudes, ConsultaCategoriaSolicitudPort categorias,
                                 DefinirZonaUrgenciaUseCase definirZonaUrgencia, PublicadorEventos eventos) {
        this.solicitudes = solicitudes;
        this.categorias = categorias;
        this.definirZonaUrgencia = definirZonaUrgencia;
        this.eventos = eventos;
    }

    public SolicitudResult crear(CrearSolicitudCommand command, EstadoSolicitud estado) {
        boolean borrador = estado == EstadoSolicitud.BORRADOR;
        String descripcion = command.descripcion() == null ? null : command.descripcion().trim();
        if (!borrador && (descripcion == null || descripcion.isBlank())) {
            throw new DatosSolicitudInvalidosException("DESCRIPCION_REQUERIDA", "La descripción es obligatoria.");
        }
        if (borrador && descripcion != null && descripcion.isBlank()) { descripcion = null; }
        if (command.categoriaId() != null) {
            if (!categorias.estaActiva(command.categoriaId())) {
                throw new DatosSolicitudInvalidosException("CATEGORIA_INVALIDA", "La categoría no existe o no está activa.");
            }
        } else if (!borrador) {
            throw new DatosSolicitudInvalidosException("CATEGORIA_INVALIDA", "La categoría es obligatoria para publicar la solicitud.");
        }
        var ubicacion = definirZonaUrgencia.ejecutar(new DefinirZonaUrgenciaCommand(command.zonaId(), command.urgencia(), !borrador));
        OffsetDateTime ahora = OffsetDateTime.now();
        UUID id = UUID.randomUUID();
        Solicitud solicitud = new Solicitud(id, command.clienteId(), command.categoriaId(), ubicacion.zonaId(), descripcion,
                ubicacion.urgencia(), estado, ahora, ahora, 0, List.of(),
                List.of(new HistorialEstadoSolicitud(UUID.randomUUID(), id, null, estado, ahora)));
        Solicitud guardada = solicitudes.guardar(solicitud);
        if (estado == EstadoSolicitud.PUBLICADA) {
            eventos.publicar(new Eventos.SolicitudPublicada(guardada.id(), command.clienteId(), guardada.categoriaId(),
                    guardada.zonaId(), guardada.urgencia().name(), ahora));
        }
        return SolicitudResult.desde(guardada);
    }

}
