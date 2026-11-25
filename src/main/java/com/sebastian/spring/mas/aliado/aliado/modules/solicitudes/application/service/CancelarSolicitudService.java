package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service;

import java.time.OffsetDateTime;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.CancelarSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.CancelarSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.repository.SolicitudRepository;
public class CancelarSolicitudService implements CancelarSolicitudUseCase {
    private final SolicitudRepository solicitudesRepository;
    public CancelarSolicitudService(SolicitudRepository solicitudesRepository) { this.solicitudesRepository = solicitudesRepository; }
    @Override
    public SolicitudResult ejecutar(CancelarSolicitudCommand command) {
        var actual = AccesoSolicitud.propiaAbierta(solicitudesRepository, command.solicitudId(), command.clienteId());
        return SolicitudResult.desde(solicitudesRepository.guardar(actual.transitarA(EstadoSolicitud.CANCELADA, OffsetDateTime.now())));
    }
}
