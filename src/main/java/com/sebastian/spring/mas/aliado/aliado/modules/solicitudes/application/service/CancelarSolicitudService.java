package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service;

import java.time.OffsetDateTime;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.CancelarSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.CancelarSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.repository.SolicitudRepository;
public class CancelarSolicitudService implements CancelarSolicitudUseCase {
    private final SolicitudRepository solicitudes;
    public CancelarSolicitudService(SolicitudRepository solicitudes) { this.solicitudes = solicitudes; }
    @Override
    public SolicitudResult ejecutar(CancelarSolicitudCommand command) {
        var actual = AccesoSolicitud.propiaAbierta(solicitudes, command.solicitudId(), command.clienteId());
        return SolicitudResult.desde(solicitudes.guardar(actual.transitarA(EstadoSolicitud.CANCELADA, OffsetDateTime.now())));
    }
}
