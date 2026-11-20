package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.GuardarBorradorCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;

public interface GuardarBorradorUseCase {
    SolicitudResult ejecutar(GuardarBorradorCommand command);
}
