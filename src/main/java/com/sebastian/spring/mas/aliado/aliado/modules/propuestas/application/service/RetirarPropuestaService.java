package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.exception.DatosPropuestaInvalidosException;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.repository.PropuestaRepository;
import java.time.OffsetDateTime;
public class RetirarPropuestaService implements RetirarPropuestaUseCase {
    private final PropuestaRepository propuestas;
    
    public RetirarPropuestaService(PropuestaRepository propuestas) {
        this.propuestas = propuestas; 
    }
    @Override public PropuestaResult ejecutar(RetirarPropuestaCommand command) {
        var actual = AccesoPropuesta.propiaActiva(propuestas, command.propuestaId(), command.prestadorId());
        return PropuestaResult.desde(propuestas.guardar(actual.retirar(OffsetDateTime.now())));
    }
}
