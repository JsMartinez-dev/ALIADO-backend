package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.EliminacionResult;
import java.util.UUID;

public interface SolicitarEliminacionCuentaUseCase {
    EliminacionResult ejecutar(UUID usuarioId);
}
