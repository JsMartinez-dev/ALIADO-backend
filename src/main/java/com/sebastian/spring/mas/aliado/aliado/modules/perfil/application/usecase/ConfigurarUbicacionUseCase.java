package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.ConfigurarUbicacionCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.PerfilResult;

public interface ConfigurarUbicacionUseCase {
    PerfilResult ejecutar(ConfigurarUbicacionCommand command);
}
