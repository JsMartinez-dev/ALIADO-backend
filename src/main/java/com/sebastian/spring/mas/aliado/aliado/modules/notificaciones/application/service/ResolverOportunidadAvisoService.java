package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.OportunidadDesdeAvisoResult;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResolverOportunidadAvisoCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.EstadoOportunidadAvisoPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ResolverOportunidadAvisoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoOportunidadAviso;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import java.util.Objects;

/**
 * Estado permitido al destinatario de un aviso de oportunidad (F09-PUSH-10).
 *
 * <p>Antes de consultar la solicitud se comprueba que el aviso pertenezca a la cuenta
 * autenticada y sea de tipo {@code NUEVA_OPORTUNIDAD}. Todos los rechazos usan el mismo
 * error genérico, de modo que no se revela si el aviso existe, a quién pertenece o qué
 * tipo tiene.</p>
 */
public class ResolverOportunidadAvisoService implements ResolverOportunidadAvisoUseCase {

    private final NotificacionRepository notificaciones;
    private final EstadoOportunidadAvisoPort estadoOportunidad;

    public ResolverOportunidadAvisoService(NotificacionRepository notificaciones,
                                           EstadoOportunidadAvisoPort estadoOportunidad) {
        this.notificaciones = Objects.requireNonNull(notificaciones);
        this.estadoOportunidad = Objects.requireNonNull(estadoOportunidad);
    }

    @Override
    public OportunidadDesdeAvisoResult ejecutar(ResolverOportunidadAvisoCommand command) {
        Notificacion aviso = notificaciones.porId(command.notificacionId()).orElseThrow(this::noDisponible);
        if (!aviso.destinatarioId().equals(command.prestadorId())
                || aviso.tipo() != TipoNotificacion.NUEVA_OPORTUNIDAD) {
            throw noDisponible();
        }
        EstadoOportunidadAviso estado = estadoOportunidad.estado(aviso.aggregateId(), command.prestadorId())
                .orElseThrow(this::noDisponible);
        return new OportunidadDesdeAvisoResult(aviso.aggregateId(), estado);
    }

    private BusinessException noDisponible() {
        return new BusinessException.NotFound("OPORTUNIDAD_NO_DISPONIBLE", "Oportunidad no disponible.");
    }
}
