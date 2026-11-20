package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.ActualizarZonasAtencionCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult;
public interface ActualizarZonasAtencionUseCase {
    PerfilProfesionalResult ejecutar(ActualizarZonasAtencionCommand command);
}
