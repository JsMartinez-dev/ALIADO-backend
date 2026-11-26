package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.usecase.ValidarDisponibilidadPropuestaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.exception.DatosPropuestaInvalidosException;
public class ValidarDisponibilidadPropuestaService implements ValidarDisponibilidadPropuestaUseCase {
    @Override public String ejecutar(String disponibilidad) {
        if (disponibilidad == null || disponibilidad.isBlank()) {
            throw new DatosPropuestaInvalidosException("DISPONIBILIDAD_REQUERIDA", "La disponibilidad es obligatoria en toda propuesta.");
        }
        if (disponibilidad.length() > 255) {
            throw new DatosPropuestaInvalidosException("DISPONIBILIDAD_MUY_LARGA", "La disponibilidad admite máximo 255 caracteres.");
        }
        return disponibilidad.trim();
    }
}
