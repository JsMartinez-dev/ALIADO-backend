package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.PreferenciaResult;
import java.util.UUID;
import java.util.List;

public interface ListarPreferenciasUseCase {
    List<PreferenciaResult> ejecutar(UUID usuarioId);
}
