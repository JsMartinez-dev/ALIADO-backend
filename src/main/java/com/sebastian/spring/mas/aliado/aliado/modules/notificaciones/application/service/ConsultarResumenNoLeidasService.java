package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResumenNoLeidasResult;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarResumenNoLeidasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import java.util.Objects;
import java.util.UUID;

/**
 * Indicador de la campana (F09-BANDEJA-03): una sola consulta de existencia por
 * destinatario y {@code leida_en IS NULL}, sin cargar la bandeja.
 */
public class ConsultarResumenNoLeidasService implements ConsultarResumenNoLeidasUseCase {

    private final NotificacionRepository notificaciones;

    public ConsultarResumenNoLeidasService(NotificacionRepository notificaciones) {
        this.notificaciones = Objects.requireNonNull(notificaciones);
    }

    @Override
    public ResumenNoLeidasResult ejecutar(UUID destinatarioId) {
        if (destinatarioId == null) {
            throw new BusinessException.BadRequest("DATOS_INVALIDOS", "Datos inválidos.");
        }
        return new ResumenNoLeidasResult(notificaciones.hayNoLeidas(destinatarioId));
    }
}
