package com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.dto.PrestadorPublicoResult;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.exception.PerfilPublicoNoDisponibleException;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.port.ConsultaDescubrimientoPort;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.usecase.ConsultarPerfilPublicoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Visibilidad;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.EstadoExposicion;
import java.util.UUID;

public class ConsultarPerfilPublicoService implements ConsultarPerfilPublicoUseCase {
    private final ConsultaDescubrimientoPort consultaDescubrimientoPort;
    private final ProyeccionPrestadorPublico proyeccionPrestadorPublico;

    public ConsultarPerfilPublicoService(ConsultaDescubrimientoPort consultaDescubrimientoPort,
            ProyeccionPrestadorPublico proyeccionPrestadorPublico) {
        this.consultaDescubrimientoPort = consultaDescubrimientoPort;
        this.proyeccionPrestadorPublico = proyeccionPrestadorPublico;
    }

    @Override
    public PrestadorPublicoResult ejecutar(UUID prestadorId) {
        if (consultaDescubrimientoPort.preferenciaDe(prestadorId) == Visibilidad.PRIVADO) {
            throw new PerfilPublicoNoDisponibleException();
        }
        var perfilProfesional = consultaDescubrimientoPort.porPrestadorId(prestadorId)
                .filter(perfil -> perfil.estadoExposicion() == EstadoExposicion.PUBLICO)
                .filter(perfil -> !perfil.disponibilidades().isEmpty())
                .orElseThrow(PerfilPublicoNoDisponibleException::new);
        return proyeccionPrestadorPublico.resultado(perfilProfesional);
    }
}
