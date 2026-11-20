package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service;

import java.util.*;
import java.time.*;
import java.math.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase.ConsultarConfianzaUseCase;

public class ConsultarConfianzaService implements ConsultarConfianzaUseCase {
    private final OperacionesConfianza operaciones;
    public ConsultarConfianzaService(OperacionesConfianza operaciones) { this.operaciones = operaciones; }
    @Override
    public ConfianzaResult ejecutar(ConsultarConfianzaCommand command) {
        var resultado = operaciones.confianzaDe(command.prestadorId());
        return new ConfianzaResult(resultado.reputacion().valor(), resultado.reputacion().cantidad(), resultado.nivelVerificacion().name());
    }
}
