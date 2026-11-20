package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.CrearSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;

public interface CrearSolicitudUseCase {
    SolicitudResult ejecutar(CrearSolicitudCommand command);
}
