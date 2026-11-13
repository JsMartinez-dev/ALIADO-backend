package com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.ConfirmarRecuperacionCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase.ConfirmarRecuperacionAccesoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.HasheadorPassword;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.HuellaToken;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.SesionPort;
import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.TokenRecuperacion;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.Usuario;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.TokenRecuperacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.UsuarioRepository;
import java.time.Clock;
import java.time.OffsetDateTime;

public class ConfirmarRecuperacionAccesoService implements ConfirmarRecuperacionAccesoUseCase {
    private final TokenRecuperacionRepository tokens;
    private final UsuarioRepository usuarios;
    private final HasheadorPassword passwords;
    private final HuellaToken huella;
    private final SesionPort sesiones;
    private final Clock clock;

    public ConfirmarRecuperacionAccesoService(TokenRecuperacionRepository tokens, UsuarioRepository usuarios,
            HasheadorPassword passwords, HuellaToken huella, SesionPort sesiones, Clock clock) {
        this.tokens = tokens; this.usuarios = usuarios; this.passwords = passwords;
        this.huella = huella; this.sesiones = sesiones; this.clock = clock;
    }

    @Override
    public void ejecutar(ConfirmarRecuperacionCommand command) {
        OffsetDateTime ahora = OffsetDateTime.now(clock);
        TokenRecuperacion token = tokens.porCodigoHash(huella.calcular(command.token()))
                .orElseThrow(() -> new BusinessException.BadRequest("TOKEN_INVALIDO", "El código es inválido."));
        if (!token.vigente(ahora)) {
            throw new BusinessException.Gone("TOKEN_EXPIRADO", "El código ya fue usado o expiró.");
        }
        try {
            Usuario.validarPassword(command.nuevaPassword());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException.BadRequest("PASSWORD_INVALIDA", ex.getMessage());
        }
        Usuario usuario = usuarios.porId(token.usuarioId())
                .orElseThrow(() -> new BusinessException.NotFound("USUARIO_NO_ENCONTRADO", "Usuario no encontrado."));
        tokens.guardar(new TokenRecuperacion(token.id(), token.usuarioId(), token.codigoHash(),
                token.fechaCreacion(), token.fechaExpiracion(), true));
        usuarios.guardar(new Usuario(usuario.id(), usuario.email(), passwords.hashear(command.nuevaPassword()),
                usuario.rol(), usuario.estadoCuenta(), usuario.fechaCreacion()));
        sesiones.cerrarSesionesDeUsuario(usuario.id());
    }
}
