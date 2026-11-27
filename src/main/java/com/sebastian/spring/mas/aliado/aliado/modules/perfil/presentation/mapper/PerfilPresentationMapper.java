package com.sebastian.spring.mas.aliado.aliado.modules.perfil.presentation.mapper;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.PerfilResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.ActualizarPerfilCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.ConfigurarUbicacionCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.ConfigurarPrivacidadCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.PerfilPublicoResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.EliminacionResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.presentation.dto.PerfilDtos;
import java.util.UUID;

public final class PerfilPresentationMapper {
    private PerfilPresentationMapper() {}

    public static PerfilDtos.EliminacionResponse aResponse(EliminacionResult resultado) {
        return new PerfilDtos.EliminacionResponse(resultado.estado(), resultado.expiraEn(),
                resultado.archivosPendientes(), "DEFINITIVA_MVP_V1");
    }

    public static PerfilDtos.PerfilPublicoResponse aResponse(PerfilPublicoResult resultado) {
        return new PerfilDtos.PerfilPublicoResponse(resultado.usuarioId(), resultado.zonaAproximada());
    }

    public static ConfigurarPrivacidadCommand aCommand(UUID usuarioId, PerfilDtos.VisibilidadRequest request) {
        return new ConfigurarPrivacidadCommand(usuarioId, request.preferenciaVisibilidad());
    }

    public static ConfigurarUbicacionCommand aCommand(UUID usuarioId, PerfilDtos.UbicacionRequest request) {
        return new ConfigurarUbicacionCommand(usuarioId, request.zonaAproximada(), request.direccionExacta());
    }

    public static ActualizarPerfilCommand aCommand(UUID usuarioId, PerfilDtos.ActualizarPerfilRequest request) {
        return new ActualizarPerfilCommand(usuarioId, request.nombre(), request.telefono(), request.version());
    }

    public static PerfilDtos.PerfilResponse aResponse(PerfilResult resultado) {
        return new PerfilDtos.PerfilResponse(resultado.usuarioId(), resultado.nombre(), resultado.telefono(),
                resultado.preferenciaVisibilidad(), resultado.zonaAproximada(),
                resultado.direccionExacta(), resultado.version());
    }
}
