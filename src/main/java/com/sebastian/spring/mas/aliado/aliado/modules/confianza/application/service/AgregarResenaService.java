package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service;

import java.util.*;
import java.time.*;
import java.math.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase.AgregarResenaUseCase;

public class AgregarResenaService implements AgregarResenaUseCase {
    private final OperacionesConfianza operaciones;
    public AgregarResenaService(OperacionesConfianza operaciones) { this.operaciones = operaciones; }
    @Override
    public UUID ejecutar(AgregarResenaCommand command) {
        var resultado = operaciones.agregarResena(command.calificacionId(), command.clienteId(), command.texto());
        return resultado.id();
    }
}
