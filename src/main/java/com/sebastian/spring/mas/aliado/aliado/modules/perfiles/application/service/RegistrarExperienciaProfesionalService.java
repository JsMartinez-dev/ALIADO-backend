package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.RegistrarExperienciaProfesionalCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult.ExperienciaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.PerfilProfesionalNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.RegistrarExperienciaProfesionalUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.ExperienciaProfesional;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
public class RegistrarExperienciaProfesionalService implements RegistrarExperienciaProfesionalUseCase {
    private final PerfilProfesionalRepository perfiles;
    public RegistrarExperienciaProfesionalService(PerfilProfesionalRepository perfiles) { this.perfiles = perfiles; }
    @Override
    public ExperienciaResult ejecutar(RegistrarExperienciaProfesionalCommand command) {
        var actual = perfiles.porPrestadorId(command.prestadorId()).orElseThrow(PerfilProfesionalNoEncontradoException::new);
        new ExperienciaProfesional(null, null, command.descripcion(), command.fechaInicio(), command.fechaFin());
        var experiencia = new ExperienciaProfesional(UUID.randomUUID(), actual.id(),
                command.descripcion().trim(), command.fechaInicio(), command.fechaFin());
        var lista = new ArrayList<>(actual.experiencias());
        lista.add(experiencia);
        perfiles.guardar(actual.conExperiencias(List.copyOf(lista)));
        return new ExperienciaResult(experiencia.id(), experiencia.descripcion(), experiencia.fechaInicio(), experiencia.fechaFin());
    }
}
