package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.ActualizarCategoriasProfesionalesCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult;
public interface ActualizarCategoriasProfesionalesUseCase {
    PerfilProfesionalResult ejecutar(ActualizarCategoriasProfesionalesCommand command);
}
