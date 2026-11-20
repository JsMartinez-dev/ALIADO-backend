package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.RegistrarExperienciaProfesionalCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult.ExperienciaResult;
public interface RegistrarExperienciaProfesionalUseCase {
    ExperienciaResult ejecutar(RegistrarExperienciaProfesionalCommand command);
}
