package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.ConfigurarPrivacidadCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.PerfilResult;

public interface ConfigurarPrivacidadUseCase {
    PerfilResult ejecutar(ConfigurarPrivacidadCommand command);
}
