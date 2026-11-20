package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.GuardarFotoSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.FotoSolicitudResult;
public interface ReemplazarFotoSolicitudUseCase {
    FotoSolicitudResult ejecutar(GuardarFotoSolicitudCommand command);
}
