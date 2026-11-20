package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.PerfilPublicoResult;
import java.util.UUID;

public interface ConsultarPerfilPublicoUseCase {
    PerfilPublicoResult ejecutar(UUID usuarioId);
}
