package com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.dto.PrestadorPublicoResult;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.exception.PerfilPublicoNoDisponibleException;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.port.ConsultaDescubrimientoPort;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.usecase.ConsultarPerfilPublicoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Visibilidad;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.EstadoExposicion;
import java.util.UUID;

public class ConsultarPerfilPublicoService implements ConsultarPerfilPublicoUseCase {
    private final ConsultaDescubrimientoPort consultas;
    private final ProyeccionPrestadorPublico proyeccion;

    public ConsultarPerfilPublicoService(ConsultaDescubrimientoPort consultas, ProyeccionPrestadorPublico proyeccion) {
        this.consultas = consultas;
        this.proyeccion = proyeccion;
    }

    @Override
    public PrestadorPublicoResult ejecutar(UUID prestadorId) {
        if (consultas.preferenciaDe(prestadorId) == Visibilidad.PRIVADO) {
            throw new PerfilPublicoNoDisponibleException();
        }
        var perfil = consultas.porPrestadorId(prestadorId)
                .filter(p -> p.estadoExposicion() == EstadoExposicion.PUBLICO)
                .filter(p -> !p.disponibilidades().isEmpty())
                .orElseThrow(PerfilPublicoNoDisponibleException::new);
        return proyeccion.resultado(perfil);
    }
}

