package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.ActualizarPerfilCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.PerfilResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.ConflictoVersionPerfilException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.DatosPerfilInvalidosException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.PerfilNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ActualizarPerfilUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConsultarPerfilUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Perfil;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.PerfilRepository;

public class ActualizarPerfilService implements ActualizarPerfilUseCase {
    private final PerfilRepository perfiles;
    private final ConsultarPerfilUseCase consultarPerfil;

    public ActualizarPerfilService(PerfilRepository perfiles, ConsultarPerfilUseCase consultarPerfil) {
        this.perfiles = perfiles;
        this.consultarPerfil = consultarPerfil;
    }

    @Override
    public PerfilResult ejecutar(ActualizarPerfilCommand command) {
        Perfil actual = perfiles.porUsuarioId(command.usuarioId())
                .orElseThrow(PerfilNoEncontradoException::new);
        if (command.version() != null && command.version() != actual.version()) {
            throw new ConflictoVersionPerfilException();
        }
        String nombre = command.nombre();
        String telefono = command.telefono();
        if (nombre != null && nombre.isBlank()) {
            throw new DatosPerfilInvalidosException("NOMBRE_INVALIDO", "El nombre no puede estar vacío.");
        }
        if (nombre != null && nombre.trim().length() > 120) {
            throw new DatosPerfilInvalidosException("NOMBRE_MUY_LARGO", "El nombre admite máximo 120 caracteres.");
        }
        if (telefono != null && !telefono.matches("[+0-9 ()-]{7,30}")) {
            throw new DatosPerfilInvalidosException("TELEFONO_INVALIDO", "El teléfono tiene un formato inválido.");
        }
        perfiles.guardar(new Perfil(command.usuarioId(),
                nombre == null ? actual.nombre() : nombre,
                telefono == null ? actual.telefono() : telefono,
                actual.preferenciaVisibilidad(), actual.version()));
        return consultarPerfil.ejecutar(command.usuarioId());
    }
}
