package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.EditarPerfilProfesionalCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult;
public interface EditarPerfilProfesionalUseCase {
    PerfilProfesionalResult ejecutar(EditarPerfilProfesionalCommand command);
}
