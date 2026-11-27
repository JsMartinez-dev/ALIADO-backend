package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.CalificacionPropiaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.ConsultarCalificacionPropiaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase.ConsultarCalificacionPropiaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.CalificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.ResenaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.ServicioLecturaPort;

public class ConsultarCalificacionPropiaService implements ConsultarCalificacionPropiaUseCase {
    private final CalificacionRepository calificaciones;
    private final ResenaRepository resenas;
    private final ServicioLecturaPort servicios;

    public ConsultarCalificacionPropiaService(CalificacionRepository calificaciones,
                                               ResenaRepository resenas,
                                               ServicioLecturaPort servicios) {
        this.calificaciones = calificaciones;
        this.resenas = resenas;
        this.servicios = servicios;
    }

    @Override
    public CalificacionPropiaResult ejecutar(ConsultarCalificacionPropiaCommand command) {
        var servicio = servicios.porId(command.servicioId())
                .orElseThrow(() -> new BusinessException.NotFound("SERVICIO_NO_ENCONTRADO",
                        "Servicio no encontrado."));
        if (!servicio.clienteId().equals(command.clienteId())) {
            throw new BusinessException.Forbidden("NO_AUTORIZADO",
                    "Solo el Cliente del servicio puede consultar su calificación.");
        }
        var calificacion = calificaciones.porServicioId(command.servicioId())
                .orElseThrow(() -> new BusinessException.NotFound("CALIFICACION_NO_ENCONTRADA",
                        "El servicio aún no tiene calificación."));
        return new CalificacionPropiaResult(calificacion.id(), calificacion.valor(),
                resenas.porCalificacionId(calificacion.id()).isPresent());
    }
}
