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
    private final SolicitudEliminacionRepository eliminaciones;
    private final ConsultaServiciosActivosPort servicios;
    private final PublicadorEventos eventos;

    public ConfirmarEliminacionCuentaService(SolicitudEliminacionRepository eliminaciones,
                                            ConsultaServiciosActivosPort servicios, PublicadorEventos eventos) {
        this.eliminaciones = eliminaciones;
        this.servicios = servicios;
        this.eventos = eventos;
    }

    @Override
    public EliminacionResult ejecutar(UUID usuarioId) {
        SolicitudEliminacion solicitud = eliminaciones.ultimaDeUsuario(usuarioId)
                .orElseThrow(SinSolicitudPendienteException::new);
        if (solicitud.estado() == EstadoEliminacion.EXPIRADA) {
            return new EliminacionResult(EstadoEliminacion.EXPIRADA.name(), solicitud.fechaExpiracion());
        }
        if (solicitud.estado() != EstadoEliminacion.PENDIENTE) {
            throw new SinSolicitudPendienteException();
        }
        OffsetDateTime ahora = OffsetDateTime.now();
        if (solicitud.vencida(ahora)) {
            if (!eliminaciones.transicionar(solicitud.id(), EstadoEliminacion.PENDIENTE,
                    EstadoEliminacion.EXPIRADA, null)) {
                return resolverCambioConcurrente(usuarioId);
            }
            // Se devuelve el estado para que infraestructura confirme la transacción antes de producir el error 410.
            return new EliminacionResult(EstadoEliminacion.EXPIRADA.name(), solicitud.fechaExpiracion());
        }
        if (servicios.bloqueaEliminacion(usuarioId)) {
            throw new ServicioBloqueaEliminacionException();
        }
        if (!eliminaciones.transicionar(solicitud.id(), EstadoEliminacion.PENDIENTE,
                EstadoEliminacion.CONFIRMADA, ahora)) {
            return resolverCambioConcurrente(usuarioId);
        }
        eventos.publicar(new Eventos.EliminacionConfirmada(usuarioId, ahora));
        return new EliminacionResult(EstadoEliminacion.CONFIRMADA.name(), solicitud.fechaExpiracion());
    }

    private EliminacionResult resolverCambioConcurrente(UUID usuarioId) {
        SolicitudEliminacion actual = eliminaciones.ultimaDeUsuario(usuarioId)
                .orElseThrow(SinSolicitudPendienteException::new);
        if (actual.estado() == EstadoEliminacion.EXPIRADA) {
            return new EliminacionResult(EstadoEliminacion.EXPIRADA.name(), actual.fechaExpiracion());
        }
        throw new ConflictoEliminacionException();
    }
}
