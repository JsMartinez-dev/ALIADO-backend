package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult;
import java.util.UUID;

public interface ConsultarPerfilPropioUseCase {
    PerfilProfesionalResult ejecutar(UUID prestadorId);
}
