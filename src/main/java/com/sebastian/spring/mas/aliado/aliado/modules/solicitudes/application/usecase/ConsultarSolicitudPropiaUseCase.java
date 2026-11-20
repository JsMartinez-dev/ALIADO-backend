package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.ConsultarSolicitudPropiaQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;
public interface ConsultarSolicitudPropiaUseCase {
    SolicitudResult ejecutar(ConsultarSolicitudPropiaQuery query);
}
