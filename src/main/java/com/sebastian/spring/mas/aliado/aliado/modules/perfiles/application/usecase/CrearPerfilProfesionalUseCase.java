package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.CrearPerfilProfesionalCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult;

public interface CrearPerfilProfesionalUseCase {
    PerfilProfesionalResult ejecutar(CrearPerfilProfesionalCommand command);
}
