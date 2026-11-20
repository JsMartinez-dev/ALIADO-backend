package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.ActualizarZonasAtencionCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.PerfilProfesionalNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.ConsultaCatalogoPerfilPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ActualizarZonasAtencionUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
public class ActualizarZonasAtencionService implements ActualizarZonasAtencionUseCase {
    private final PerfilProfesionalRepository perfiles;
    private final ConsultaCatalogoPerfilPort catalogo;
    public ActualizarZonasAtencionService(PerfilProfesionalRepository perfiles, ConsultaCatalogoPerfilPort catalogo) {
        this.perfiles = perfiles;
        this.catalogo = catalogo;
    }
    @Override
    public PerfilProfesionalResult ejecutar(ActualizarZonasAtencionCommand command) {
        var actual = perfiles.porPrestadorId(command.prestadorId()).orElseThrow(PerfilProfesionalNoEncontradoException::new);
        var zonas = CatalogoPerfilValidacion.validarActivos(command.zonaIds(), "zona de atención", catalogo::zonaActiva);
        return PerfilProfesionalResult.desde(perfiles.guardar(actual.conZonas(zonas)));
    }
}
