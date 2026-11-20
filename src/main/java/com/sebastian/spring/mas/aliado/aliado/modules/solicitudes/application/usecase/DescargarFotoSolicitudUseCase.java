package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.ConsultarFotoSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.DescargaFotoResult;
public interface DescargarFotoSolicitudUseCase {
    DescargaFotoResult ejecutar(ConsultarFotoSolicitudCommand command);
}
