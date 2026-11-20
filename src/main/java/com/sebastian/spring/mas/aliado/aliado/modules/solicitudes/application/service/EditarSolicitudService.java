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
    private final SolicitudRepository solicitudes;
    private final ConsultaCategoriaSolicitudPort categorias;
    private final ConsultaZonaSolicitudPort zonas;
    public EditarSolicitudService(SolicitudRepository solicitudes, ConsultaCategoriaSolicitudPort categorias,
            ConsultaZonaSolicitudPort zonas) {
        this.solicitudes = solicitudes; this.categorias = categorias; this.zonas = zonas;
    }
    @Override
    public SolicitudResult ejecutar(EditarSolicitudCommand command) {
        Solicitud actual = AccesoSolicitud.propiaAbierta(solicitudes, command.solicitudId(), command.clienteId());
        var categoria = actual.categoriaId();
        var zona = actual.zonaId();
        Urgencia urgencia = actual.urgencia();
        if (command.categoriaId() != null) {
            if (!categorias.estaActiva(command.categoriaId())) {
                throw new DatosSolicitudInvalidosException("CATEGORIA_INVALIDA", "La categoría no existe o no está activa.");
            }
            categoria = command.categoriaId();
        }
        if (command.zonaId() != null) {
            if (!zonas.estaActiva(command.zonaId())) {
                throw new DatosSolicitudInvalidosException("ZONA_INVALIDA", "La zona no existe o no está activa.");
            }
            zona = command.zonaId();
        }
        if (command.urgencia() != null) {
            try { urgencia = Urgencia.valueOf(command.urgencia()); }
            catch (IllegalArgumentException ex) {
                throw new DatosSolicitudInvalidosException("URGENCIA_INVALIDA", "Urgencia inválida.");
            }
        }
        String descripcion = command.descripcion() == null ? actual.descripcion() : command.descripcion().trim();
        if (actual.estado() == EstadoSolicitud.PUBLICADA && (descripcion == null || descripcion.isBlank())) {
            throw new DatosSolicitudInvalidosException("DESCRIPCION_REQUERIDA", "La descripción es obligatoria.");
        }
        Solicitud editada = new Solicitud(actual.id(), command.clienteId(), categoria, zona, descripcion, urgencia,
                actual.estado(), actual.fechaCreacion(), OffsetDateTime.now(), actual.version(), actual.fotos(), actual.historial());
        return SolicitudResult.desde(solicitudes.guardar(editada));
    }
}
