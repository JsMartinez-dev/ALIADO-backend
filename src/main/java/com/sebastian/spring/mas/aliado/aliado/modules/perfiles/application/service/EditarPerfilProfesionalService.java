package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.EditarPerfilProfesionalCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.PerfilProfesionalNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.EditarPerfilProfesionalUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.DatosGeneralesProfesional;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
public class EditarPerfilProfesionalService implements EditarPerfilProfesionalUseCase {
    private final PerfilProfesionalRepository perfiles;
    public EditarPerfilProfesionalService(PerfilProfesionalRepository perfiles) { this.perfiles = perfiles; }
    @Override
    public PerfilProfesionalResult ejecutar(EditarPerfilProfesionalCommand command) {
        var actual = perfiles.porPrestadorId(command.prestadorId()).orElseThrow(PerfilProfesionalNoEncontradoException::new);
        return PerfilProfesionalResult.desde(perfiles.guardar(actual.conDatos(
                DatosGeneralesProfesional.fusionar(actual.datosGenerales(), command.descripcion()))));
    }
}
