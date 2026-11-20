package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.CrearSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.GuardarBorradorCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.DatosSolicitudInvalidosException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.CrearSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.GuardarBorradorUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;

/** Entrada de creación que conserva la selección de estado del contrato existente. */
public class CrearSolicitudService implements CrearSolicitudUseCase {
    private final CreacionSolicitud creacion;
    private final GuardarBorradorUseCase guardarBorrador;

    public CrearSolicitudService(CreacionSolicitud creacion, GuardarBorradorUseCase guardarBorrador) {
        this.creacion = creacion;
        this.guardarBorrador = guardarBorrador;
    }

    @Override
    public SolicitudResult ejecutar(CrearSolicitudCommand command) {
        EstadoSolicitud estado = resolverEstado(command.estadoDeseado());
        if (estado == EstadoSolicitud.BORRADOR) {
            return guardarBorrador.ejecutar(new GuardarBorradorCommand(command.clienteId(), command.categoriaId(),
                    command.descripcion(), command.zonaId(), command.urgencia()));
        }
        return creacion.crear(command, EstadoSolicitud.PUBLICADA);
    }

    private EstadoSolicitud resolverEstado(String texto) {
        if (texto == null || texto.isBlank()) { return EstadoSolicitud.PUBLICADA; }
        try {
            EstadoSolicitud estado = EstadoSolicitud.valueOf(texto);
            if (estado == EstadoSolicitud.BORRADOR || estado == EstadoSolicitud.PUBLICADA) { return estado; }
        } catch (IllegalArgumentException ex) {
            throw estadoInvalido();
        }
        throw estadoInvalido();
    }

    private DatosSolicitudInvalidosException estadoInvalido() {
        return new DatosSolicitudInvalidosException("ESTADO_INVALIDO", "estadoDeseado debe ser BORRADOR o PUBLICADA.");
    }
}
