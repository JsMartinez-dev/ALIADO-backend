package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.PerfilResult;
import java.util.UUID;

public interface ConsultarPerfilUseCase {
    PerfilResult ejecutar(UUID usuarioId);
}
