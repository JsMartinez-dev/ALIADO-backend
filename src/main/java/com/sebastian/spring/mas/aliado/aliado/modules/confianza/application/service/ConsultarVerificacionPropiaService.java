package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service;

import java.util.*;
import java.time.*;
import java.math.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase.ConsultarVerificacionPropiaUseCase;

public class ConsultarVerificacionPropiaService implements ConsultarVerificacionPropiaUseCase {
    private final OperacionesVerificacion operaciones;
    public ConsultarVerificacionPropiaService(OperacionesVerificacion operaciones) { this.operaciones = operaciones; }
    @Override
    public VerificacionResult ejecutar(ConsultarVerificacionPropiaCommand command) {
        var resultado = operaciones.miVerificacion(command.prestadorId());
        return new VerificacionResult(resultado.id(), resultado.estado().name());
    }
}
