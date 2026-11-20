package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.HistorialQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.ServicioResult;
import java.util.List;

public interface ConsultarHistorialUseCase {
    List<ServicioResult> ejecutar(HistorialQuery query);
}
