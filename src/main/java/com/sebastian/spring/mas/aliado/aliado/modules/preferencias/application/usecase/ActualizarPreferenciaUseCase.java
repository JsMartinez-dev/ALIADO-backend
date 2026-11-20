package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.PreferenciaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.ActualizarPreferenciaCommand;

public interface ActualizarPreferenciaUseCase {
    PreferenciaResult ejecutar(ActualizarPreferenciaCommand command);
}
