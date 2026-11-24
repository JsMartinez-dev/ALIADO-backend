package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.CrearPerfilProfesionalCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.ExperienciaDatos;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.PerfilProfesionalYaExisteException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.ConsultaCatalogoPerfilPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.CrearPerfilProfesionalUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.DatosGeneralesProfesional;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.EstadoExposicion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.ExperienciaProfesional;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CrearPerfilProfesionalService implements CrearPerfilProfesionalUseCase {
    private final PerfilProfesionalRepository perfiles;
    private final ConsultaCatalogoPerfilPort catalogo;

    public CrearPerfilProfesionalService(PerfilProfesionalRepository perfiles, ConsultaCatalogoPerfilPort catalogo) {
        this.perfiles = perfiles;
        this.catalogo = catalogo;
    }

    @Override
    public PerfilProfesionalResult ejecutar(CrearPerfilProfesionalCommand command) {
        if (perfiles.porPrestadorId(command.prestadorId()).isPresent()) {
            throw new PerfilProfesionalYaExisteException();
        }
        Set<UUID> categorias = CatalogoPerfilValidacion.validarActivos(command.categoriaIds(), "categoría", catalogo::categoriaActiva);
        Set<UUID> zonas = CatalogoPerfilValidacion.validarActivos(command.zonaIds(), "zona de atención", catalogo::zonaActiva);
        List<ExperienciaDatos> experiencias = command.experiencias() == null ? List.of() : command.experiencias();
        // Se conserva la validación de dominio y el orden de validación del flujo original.
        experiencias.forEach(e -> new ExperienciaProfesional(null, null,
                e == null ? null : e.descripcion(), e == null ? null : e.fechaInicio(), e == null ? null : e.fechaFin()));
        var experienciasDominio = experiencias.stream().map(e -> new ExperienciaProfesional(
                UUID.randomUUID(), null, e.descripcion(), e.fechaInicio(), e.fechaFin())).toList();
        OffsetDateTime ahora = OffsetDateTime.now();
        PerfilProfesional perfil = new PerfilProfesional(UUID.randomUUID(), command.prestadorId(),
                DatosGeneralesProfesional.paraCreacion(command.descripcion()),
                EstadoExposicion.PUBLICO, ahora, ahora, categorias, zonas, experienciasDominio,
                List.of(), List.of(), List.of());
        return PerfilProfesionalResult.desde(perfiles.guardar(perfil));
    }

}
