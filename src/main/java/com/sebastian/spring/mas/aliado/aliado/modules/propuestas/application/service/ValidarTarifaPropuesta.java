package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.exception.DatosPropuestaInvalidosException;

final class ValidarTarifaPropuesta {
    private ValidarTarifaPropuesta() {}

    static Integer validar(Integer tarifaCop, boolean obligatoria) {
        if (tarifaCop == null) {
            if (obligatoria) {
                throw new DatosPropuestaInvalidosException("TARIFA_REQUERIDA", "La tarifa es obligatoria en propuestas nuevas.");
            }
            return null;
        }
        if (tarifaCop <= 0) {
            throw new DatosPropuestaInvalidosException("TARIFA_INVALIDA", "La tarifa debe ser un entero positivo en pesos colombianos.");
        }
        return tarifaCop;
    }
}
