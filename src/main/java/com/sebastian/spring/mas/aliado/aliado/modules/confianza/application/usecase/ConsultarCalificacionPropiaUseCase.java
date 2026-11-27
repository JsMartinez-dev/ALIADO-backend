package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.CalificacionPropiaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.ConsultarCalificacionPropiaCommand;

public interface ConsultarCalificacionPropiaUseCase {
    CalificacionPropiaResult ejecutar(ConsultarCalificacionPropiaCommand command);
}
