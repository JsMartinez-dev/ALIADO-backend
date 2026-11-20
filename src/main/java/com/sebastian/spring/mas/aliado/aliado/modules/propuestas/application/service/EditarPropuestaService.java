package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.exception.DatosPropuestaInvalidosException;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.repository.PropuestaRepository;
import java.time.OffsetDateTime;
public class EditarPropuestaService implements EditarPropuestaUseCase {
    private final PropuestaRepository propuestas;
    private final ValidarDisponibilidadPropuestaUseCase disponibilidad;
    private final ValidarMensajePropuestaUseCase mensaje;
    public EditarPropuestaService(PropuestaRepository propuestas, ValidarDisponibilidadPropuestaUseCase disponibilidad, ValidarMensajePropuestaUseCase mensaje) {
        this.propuestas = propuestas; this.disponibilidad = disponibilidad; this.mensaje = mensaje;
    }
    @Override public PropuestaResult ejecutar(EditarPropuestaCommand command) {
        var actual = AccesoPropuesta.propiaActiva(propuestas, command.propuestaId(), command.prestadorId());
        if (command.disponibilidad() != null && command.disponibilidad().isBlank()) {
            throw new DatosPropuestaInvalidosException("DISPONIBILIDAD_INVALIDA", "La disponibilidad no puede estar vacía.");
        }
        String nuevaDisponibilidad = command.disponibilidad() == null ? null : disponibilidad.ejecutar(command.disponibilidad());
        String nuevoMensaje = mensaje.ejecutar(command.mensaje());
        return PropuestaResult.desde(propuestas.guardar(actual.editada(nuevaDisponibilidad, nuevoMensaje, OffsetDateTime.now())));
    }
}
