package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.PublicarSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;

public interface PublicarSolicitudUseCase {
    SolicitudResult ejecutar(PublicarSolicitudCommand command);
}
