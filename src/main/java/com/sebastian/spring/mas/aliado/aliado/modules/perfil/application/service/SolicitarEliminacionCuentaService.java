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
    private final SolicitudEliminacionRepository eliminaciones;
    private final ConsultaServiciosActivosPort servicios;

    public SolicitarEliminacionCuentaService(SolicitudEliminacionRepository eliminaciones,
                                            ConsultaServiciosActivosPort servicios) {
        this.eliminaciones = eliminaciones;
        this.servicios = servicios;
    }

    @Override
    public EliminacionResult ejecutar(UUID usuarioId) {
        if (servicios.bloqueaEliminacion(usuarioId)) {
            throw new ServicioBloqueaEliminacionException();
        }
        OffsetDateTime ahora = OffsetDateTime.now();
        SolicitudEliminacion pendiente = eliminaciones.pendienteDeUsuario(usuarioId).orElse(null);
        if (pendiente != null) {
            if (!pendiente.vencida(ahora)) {
                throw new EliminacionPendienteException();
            }
            if (!eliminaciones.transicionar(pendiente.id(), EstadoEliminacion.PENDIENTE,
                    EstadoEliminacion.EXPIRADA, null)) {
                throw new ConflictoEliminacionException();
            }
        }
        SolicitudEliminacion solicitud = eliminaciones.guardar(new SolicitudEliminacion(UUID.randomUUID(), usuarioId,
                ahora, ahora.plusHours(24), EstadoEliminacion.PENDIENTE, null));
        return new EliminacionResult(solicitud.estado().name(), solicitud.fechaExpiracion());
    }
}
