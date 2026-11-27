package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResumenNoLeidasResult;
import java.util.UUID;

/** Existencia de avisos pendientes para la campana de la cuenta (F09-BANDEJA-03). */
public interface ConsultarResumenNoLeidasUseCase {
    ResumenNoLeidasResult ejecutar(UUID destinatarioId);
}
