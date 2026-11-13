package com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.LoginCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.ResultadoLogin;

public interface IniciarSesionUseCase {
    ResultadoLogin ejecutar(LoginCommand command);
}
