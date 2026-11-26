package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.exception.DatosPropuestaInvalidosException;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.repository.PropuestaRepository;
import java.time.OffsetDateTime;
public class EditarPropuestaService implements EditarPropuestaUseCase {
    private final PropuestaRepository propuestaRepository;
    private final ValidarDisponibilidadPropuestaUseCase validarDisponibilidadPropuestaUseCase;
    private final ValidarMensajePropuestaUseCase validarMensajePropuestaUseCase;
    public EditarPropuestaService(PropuestaRepository propuestaRepository, ValidarDisponibilidadPropuestaUseCase validarDisponibilidadPropuestaUseCase, ValidarMensajePropuestaUseCase validarMensajePropuestaUseCase) {
        this.propuestaRepository = propuestaRepository; this.validarDisponibilidadPropuestaUseCase = validarDisponibilidadPropuestaUseCase; this.validarMensajePropuestaUseCase = validarMensajePropuestaUseCase;
    }

    @Override public PropuestaResult ejecutar(EditarPropuestaCommand command) {
        var actual = AccesoPropuesta.propiaActiva(propuestaRepository, command.propuestaId(), command.prestadorId());
        if (command.disponibilidad() != null && command.disponibilidad().isBlank()) {
            throw new DatosPropuestaInvalidosException("DISPONIBILIDAD_INVALIDA", "La disponibilidad no puede estar vacía.");
        }
        String nuevaDisponibilidad = command.disponibilidad() == null ? null : validarDisponibilidadPropuestaUseCase.ejecutar(command.disponibilidad());
        String nuevoMensaje = validarMensajePropuestaUseCase.ejecutar(command.mensaje());
        return PropuestaResult.desde(propuestaRepository.guardar(actual.editada(nuevaDisponibilidad, nuevoMensaje, OffsetDateTime.now())));
    }
    
}
