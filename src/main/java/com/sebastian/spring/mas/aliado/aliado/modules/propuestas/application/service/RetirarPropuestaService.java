package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.repository.PropuestaRepository;
import java.time.OffsetDateTime;
public class RetirarPropuestaService implements RetirarPropuestaUseCase {
    private final PropuestaRepository propuestaRepository;
    
    public RetirarPropuestaService(PropuestaRepository propuestas) {
        this.propuestaRepository = propuestas;
    }
    @Override public PropuestaResult ejecutar(RetirarPropuestaCommand command) {
        var actual = AccesoPropuesta.propiaActiva(propuestaRepository, command.propuestaId(), command.prestadorId());
        return PropuestaResult.desde(propuestaRepository.guardar(actual.retirar(OffsetDateTime.now())));
    }
}
