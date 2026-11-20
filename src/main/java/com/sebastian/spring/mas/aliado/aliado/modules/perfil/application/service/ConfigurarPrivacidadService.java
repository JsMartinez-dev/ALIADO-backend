package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.ConfigurarPrivacidadCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.PerfilResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.PerfilNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.VisibilidadInvalidaException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConfigurarPrivacidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConsultarPerfilUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Perfil;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Visibilidad;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.PerfilRepository;

public class ConfigurarPrivacidadService implements ConfigurarPrivacidadUseCase {
    private final PerfilRepository perfiles;
    private final ConsultarPerfilUseCase consultarPerfil;

    public ConfigurarPrivacidadService(PerfilRepository perfiles, ConsultarPerfilUseCase consultarPerfil) {
        this.perfiles = perfiles;
        this.consultarPerfil = consultarPerfil;
    }

    @Override
    public PerfilResult ejecutar(ConfigurarPrivacidadCommand command) {
        Visibilidad visibilidad;
        try {
            visibilidad = Visibilidad.valueOf(command.preferenciaVisibilidad());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new VisibilidadInvalidaException();
        }
        Perfil actual = perfiles.porUsuarioId(command.usuarioId()).orElseThrow(PerfilNoEncontradoException::new);
        perfiles.guardar(new Perfil(command.usuarioId(), actual.nombre(), actual.telefono(), visibilidad, actual.version()));
        return consultarPerfil.ejecutar(command.usuarioId());
    }
}
