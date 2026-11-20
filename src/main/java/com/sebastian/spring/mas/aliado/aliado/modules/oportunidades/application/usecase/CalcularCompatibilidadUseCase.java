package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.domain.model.CriteriosCompatibilidad;

public interface CalcularCompatibilidadUseCase {
    boolean ejecutar(CriteriosCompatibilidad criterios);
}
