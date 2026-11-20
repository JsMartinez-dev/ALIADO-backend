package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.CalcularCompatibilidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.domain.model.CriteriosCompatibilidad;

public class CalcularCompatibilidadService implements CalcularCompatibilidadUseCase {
    @Override
    public boolean ejecutar(CriteriosCompatibilidad criterios) {
        return criterios.esCompatible();
    }
}
