package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.usecase.ValidarMensajePropuestaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.exception.DatosPropuestaInvalidosException;
/** UC041: el mensaje conserva su contenido y puede estar ausente. */
public class ValidarMensajePropuestaService implements ValidarMensajePropuestaUseCase {
    @Override public String ejecutar(String mensaje) {
        if (mensaje != null && mensaje.length() > 1000) {
            throw new DatosPropuestaInvalidosException("MENSAJE_MUY_LARGO", "El mensaje admite máximo 1000 caracteres.");
        }
        return mensaje;
    }
}
