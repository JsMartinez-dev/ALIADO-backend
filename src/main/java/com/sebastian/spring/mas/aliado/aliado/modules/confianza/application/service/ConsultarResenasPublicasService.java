package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service;

import java.util.*;
import java.time.*;
import java.math.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase.ConsultarResenasPublicasUseCase;

public class ConsultarResenasPublicasService implements ConsultarResenasPublicasUseCase {
    private final OperacionesConfianza operaciones;
    public ConsultarResenasPublicasService(OperacionesConfianza operaciones) { this.operaciones = operaciones; }
    @Override
    public List<ResenaPublicaResult> ejecutar(ConsultarResenasPublicasCommand command) {
        return operaciones.resenasVisiblesDe(command.prestadorId()).stream().map(r -> new ResenaPublicaResult(r.id(), r.valor(), r.texto(), r.fechaCreacion())).toList();
    }
}
