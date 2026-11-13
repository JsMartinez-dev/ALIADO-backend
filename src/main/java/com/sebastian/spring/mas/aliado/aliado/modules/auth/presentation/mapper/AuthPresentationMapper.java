package com.sebastian.spring.mas.aliado.aliado.modules.auth.presentation.mapper;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.RegistrarUsuarioCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.LoginCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.ResultadoLogin;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.ResultadoRegistro;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.SolicitarRecuperacionCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.ConfirmarRecuperacionCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.Rol;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.presentation.dto.AuthDtos;

/** Conversión exclusiva entre contratos HTTP y objetos del caso de uso auth. */
public final class AuthPresentationMapper {
    private AuthPresentationMapper() {}

    public static LoginCommand aCommand(AuthDtos.LoginRequest request) {
        return new LoginCommand(request.email(), request.password());
    }

    public static AuthDtos.LoginResponse aResponse(ResultadoLogin result) {
        return new AuthDtos.LoginResponse(result.sessionToken(), result.rol());
    }

    public static RegistrarUsuarioCommand aCommand(AuthDtos.RegistroRequest request) {
        try {
            return new RegistrarUsuarioCommand(request.email(), request.password(), Rol.valueOf(request.rol()),
                    request.aceptaTerminos(), request.versionTerminos());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new BusinessException.BadRequest("ROL_INVALIDO", "Rol inválido.");
        }
    }

    public static AuthDtos.RegistroResponse aResponse(ResultadoRegistro result) {
        return new AuthDtos.RegistroResponse(result.id(), result.email(), result.rol());
    }

    public static SolicitarRecuperacionCommand aCommand(AuthDtos.RecuperacionRequest request) {
        return new SolicitarRecuperacionCommand(request.email());
    }

    public static ConfirmarRecuperacionCommand aCommand(AuthDtos.ConfirmarRecuperacionRequest request) {
        return new ConfirmarRecuperacionCommand(request.token(), request.nuevaPassword());
    }
}
