package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.DescargaResult;
import java.util.UUID;

public interface ExportarHistorialUseCase {
    DescargaResult ejecutar(UUID usuarioId);
}
