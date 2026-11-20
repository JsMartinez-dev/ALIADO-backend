package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.ActualizarPerfilCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.PerfilResult;

public interface ActualizarPerfilUseCase {
    PerfilResult ejecutar(ActualizarPerfilCommand command);
}
