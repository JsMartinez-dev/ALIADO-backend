package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.CrearSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.GuardarBorradorCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.GuardarBorradorUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;

public class GuardarBorradorService implements GuardarBorradorUseCase {
    private final CreacionSolicitud creacion;

    public GuardarBorradorService(CreacionSolicitud creacion) { this.creacion = creacion; }

    @Override
    public SolicitudResult ejecutar(GuardarBorradorCommand command) {
        return creacion.crear(new CrearSolicitudCommand(command.clienteId(), command.categoriaId(), command.descripcion(),
                command.zonaId(), command.urgencia(), EstadoSolicitud.BORRADOR.name()), EstadoSolicitud.BORRADOR);
    }
}
