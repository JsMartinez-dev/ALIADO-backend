package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.ConsultarFotoSolicitudCommand;
public interface EliminarFotoSolicitudUseCase {
    void ejecutar(ConsultarFotoSolicitudCommand command);
}
