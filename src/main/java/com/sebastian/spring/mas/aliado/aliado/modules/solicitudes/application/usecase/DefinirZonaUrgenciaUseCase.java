package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.DefinirZonaUrgenciaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.ZonaUrgenciaResult;

public interface DefinirZonaUrgenciaUseCase {
    ZonaUrgenciaResult ejecutar(DefinirZonaUrgenciaCommand command);
}
