package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.events.Eventos;
import com.sebastian.spring.mas.aliado.aliado.common.events.PublicadorEventos;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.EliminacionResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.ConflictoEliminacionException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.SinSolicitudPendienteException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.ServicioBloqueaEliminacionException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.ConsultaServiciosActivosPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConfirmarEliminacionCuentaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.EstadoEliminacion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.SolicitudEliminacion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.SolicitudEliminacionRepository;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ConfirmarEliminacionCuentaService implements ConfirmarEliminacionCuentaUseCase {
    private final SolicitudEliminacionRepository solicitudesEliminacionRepository;
    private final ConsultaServiciosActivosPort consultaServiciosActivosPort;
    private final PublicadorEventos publicadorEventos;

    public ConfirmarEliminacionCuentaService(
            SolicitudEliminacionRepository solicitudesEliminacionRepository,
            ConsultaServiciosActivosPort consultaServiciosActivosPort,
            PublicadorEventos publicadorEventos) {
        this.solicitudesEliminacionRepository = solicitudesEliminacionRepository;
        this.consultaServiciosActivosPort = consultaServiciosActivosPort;
        this.publicadorEventos = publicadorEventos;
    }

    @Override
    public EliminacionResult ejecutar(UUID usuarioId) {
        SolicitudEliminacion solicitudEliminacion = solicitudesEliminacionRepository.ultimaDeUsuario(usuarioId)
                .orElseThrow(SinSolicitudPendienteException::new);
        if (solicitudEliminacion.estado() == EstadoEliminacion.EXPIRADA) {
            return new EliminacionResult(
                    EstadoEliminacion.EXPIRADA.name(), solicitudEliminacion.fechaExpiracion());
        }
        if (solicitudEliminacion.estado() != EstadoEliminacion.PENDIENTE) {
            throw new SinSolicitudPendienteException();
        }
        OffsetDateTime fechaHoraConfirmacion = OffsetDateTime.now();
        if (solicitudEliminacion.vencida(fechaHoraConfirmacion)) {
            if (!solicitudesEliminacionRepository.transicionar(
                    solicitudEliminacion.id(), EstadoEliminacion.PENDIENTE,
                    EstadoEliminacion.EXPIRADA, null)) {
                return resolverCambioConcurrente(usuarioId);
            }
            // Se devuelve el estado para que infraestructura confirme la transacción antes de producir el error 410.
            return new EliminacionResult(
                    EstadoEliminacion.EXPIRADA.name(), solicitudEliminacion.fechaExpiracion());
        }
        if (consultaServiciosActivosPort.bloqueaEliminacion(usuarioId)) {
            throw new ServicioBloqueaEliminacionException();
        }
        if (!solicitudesEliminacionRepository.transicionar(
                solicitudEliminacion.id(), EstadoEliminacion.PENDIENTE,
                EstadoEliminacion.CONFIRMADA, fechaHoraConfirmacion)) {
            return resolverCambioConcurrente(usuarioId);
        }
        publicadorEventos.publicar(new Eventos.EliminacionConfirmada(usuarioId, fechaHoraConfirmacion));
        return new EliminacionResult(
                EstadoEliminacion.CONFIRMADA.name(), solicitudEliminacion.fechaExpiracion());
    }

    private EliminacionResult resolverCambioConcurrente(UUID usuarioId) {
        SolicitudEliminacion solicitudActualizada = solicitudesEliminacionRepository.ultimaDeUsuario(usuarioId)
                .orElseThrow(SinSolicitudPendienteException::new);
        if (solicitudActualizada.estado() == EstadoEliminacion.EXPIRADA) {
            return new EliminacionResult(
                    EstadoEliminacion.EXPIRADA.name(), solicitudActualizada.fechaExpiracion());
        }
        throw new ConflictoEliminacionException();
    }
}
