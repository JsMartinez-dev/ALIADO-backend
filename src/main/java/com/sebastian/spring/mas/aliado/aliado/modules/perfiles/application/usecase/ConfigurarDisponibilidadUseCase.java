package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.ConfigurarDisponibilidadCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult;
public interface ConfigurarDisponibilidadUseCase { PerfilProfesionalResult ejecutar(ConfigurarDisponibilidadCommand command); }
