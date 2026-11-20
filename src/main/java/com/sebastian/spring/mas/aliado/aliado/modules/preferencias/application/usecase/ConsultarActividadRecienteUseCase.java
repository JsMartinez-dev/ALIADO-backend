package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.ActividadResult;
import java.util.UUID;
import java.util.List;

public interface ConsultarActividadRecienteUseCase {
    List<ActividadResult> ejecutar(UUID usuarioId);
}
