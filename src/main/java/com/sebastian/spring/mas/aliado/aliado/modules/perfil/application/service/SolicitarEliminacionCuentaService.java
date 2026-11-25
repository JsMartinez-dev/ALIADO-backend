package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.EliminacionResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.EliminacionPendienteException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.ConflictoEliminacionException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.ServicioBloqueaEliminacionException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.ConsultaServiciosActivosPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.SolicitarEliminacionCuentaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.EstadoEliminacion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.SolicitudEliminacion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.SolicitudEliminacionRepository;
import java.time.OffsetDateTime;
import java.util.UUID;

public class SolicitarEliminacionCuentaService implements SolicitarEliminacionCuentaUseCase {
    private final SolicitudEliminacionRepository solicitudesEliminacionRepository;
    private final ConsultaServiciosActivosPort consultaServiciosActivosPort;

    public SolicitarEliminacionCuentaService(
            SolicitudEliminacionRepository solicitudesEliminacionRepository,
            ConsultaServiciosActivosPort consultaServiciosActivosPort) {
        this.solicitudesEliminacionRepository = solicitudesEliminacionRepository;
        this.consultaServiciosActivosPort = consultaServiciosActivosPort;
    }

    @Override
    public EliminacionResult ejecutar(UUID usuarioId) {
        if (consultaServiciosActivosPort.bloqueaEliminacion(usuarioId)) {
            throw new ServicioBloqueaEliminacionException();
        }
        OffsetDateTime fechaHoraSolicitud = OffsetDateTime.now();
        SolicitudEliminacion solicitudPendiente =
                solicitudesEliminacionRepository.pendienteDeUsuario(usuarioId).orElse(null);
        if (solicitudPendiente != null) {
            if (!solicitudPendiente.vencida(fechaHoraSolicitud)) {
                throw new EliminacionPendienteException();
            }
            if (!solicitudesEliminacionRepository.transicionar(solicitudPendiente.id(), EstadoEliminacion.PENDIENTE,
                    EstadoEliminacion.EXPIRADA, null)) {
                throw new ConflictoEliminacionException();
            }
        }
        SolicitudEliminacion solicitud = solicitudesEliminacionRepository.guardar(
                new SolicitudEliminacion(UUID.randomUUID(), usuarioId, fechaHoraSolicitud,
                        fechaHoraSolicitud.plusHours(24), EstadoEliminacion.PENDIENTE, null));
        return new EliminacionResult(solicitud.estado().name(), solicitud.fechaExpiracion());
    }
}
