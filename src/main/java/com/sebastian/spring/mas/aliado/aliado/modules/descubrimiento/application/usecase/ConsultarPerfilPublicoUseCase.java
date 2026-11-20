package com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.dto.PrestadorPublicoResult;
import java.util.UUID;

public interface ConsultarPerfilPublicoUseCase {
    PrestadorPublicoResult ejecutar(UUID prestadorId);
}

