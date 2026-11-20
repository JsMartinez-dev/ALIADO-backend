package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service;

import java.util.*;
import java.time.*;
import java.math.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase.SolicitarVerificacionUseCase;

public class SolicitarVerificacionService implements SolicitarVerificacionUseCase {
    private final OperacionesVerificacion operaciones;
    public SolicitarVerificacionService(OperacionesVerificacion operaciones) { this.operaciones = operaciones; }
    @Override
    public VerificacionResult ejecutar(SolicitarVerificacionCommand command) {
        var resultado = operaciones.solicitarVerificacion(command.prestadorId(), command.archivos() == null ? null : command.archivos().stream().map(a -> new OperacionesVerificacion.ArchivoSubido(a.nombreOriginal(), a.tipoContenido(), a.contenido())).toList());
        return new VerificacionResult(resultado.id(), resultado.estado().name());
    }
}
