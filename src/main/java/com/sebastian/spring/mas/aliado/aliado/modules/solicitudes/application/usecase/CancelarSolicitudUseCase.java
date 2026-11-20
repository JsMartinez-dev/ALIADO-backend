package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.CancelarSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;
public interface CancelarSolicitudUseCase {
    SolicitudResult ejecutar(CancelarSolicitudCommand command);
}
