package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.IndicadoresResult;
import java.util.UUID;

public interface ConsultarIndicadoresPrestadorUseCase {
    IndicadoresResult ejecutar(UUID prestadorId);
}
