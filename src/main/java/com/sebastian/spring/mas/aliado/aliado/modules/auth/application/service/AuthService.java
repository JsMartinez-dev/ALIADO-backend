package com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.CuentaBloqueoPort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.PerfilBasePort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.RolConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.SesionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.UsuarioConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.Consentimiento;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.EstadoCuenta;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.Rol;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.TokenRecuperacion;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.Usuario;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.ConsentimientoRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.TokenRecuperacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.UsuarioRepository;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService implements UsuarioConsultaPort, RolConsultaPort, CuentaBloqueoPort {

    private final UsuarioRepository usuarioRepository;
    private final ConsentimientoRepository consentimientoRepository;
    private final TokenRecuperacionRepository tokenRecuperacionRepository;
    private final SesionPort sesionPort;
    private final PerfilBasePort perfilBasePort;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, ConsentimientoRepository consentimientoRepository,
                       TokenRecuperacionRepository tokenRecuperacionRepository, SesionPort sesionPort,
                       PerfilBasePort perfilBasePort, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.consentimientoRepository = consentimientoRepository;
        this.tokenRecuperacionRepository = tokenRecuperacionRepository;
        this.sesionPort = sesionPort;
        this.perfilBasePort = perfilBasePort;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void solicitarRecuperacion(String email) {
        String correo = Usuario.normalizarEmail(email);
        usuarioRepository.porEmail(correo).ifPresent(usuario -> {
            byte[] aleatorio = new byte[24];
            new SecureRandom().nextBytes(aleatorio);
            String codigo = HexFormat.of().formatHex(aleatorio);
            OffsetDateTime ahora = OffsetDateTime.now();
            tokenRecuperacionRepository.guardar(new TokenRecuperacion(UUID.randomUUID(), usuario.id(),
                    SesionService.hashToken(codigo), ahora, ahora.plusMinutes(30), false));
            log.warn("Código de recuperación (solo desarrollo, no exponer en API): {}", codigo);
        });
    }

    @Transactional
    public void confirmarRecuperacion(String codigo, String nuevaPassword) {
        OffsetDateTime ahora = OffsetDateTime.now();
        // Plan SPEC-01: 400 si el código no existe, 410 si fue usado o expiró.
        TokenRecuperacion token = tokenRecuperacionRepository.porCodigoHash(SesionService.hashToken(codigo))
                .orElseThrow(() -> new BusinessException.BadRequest("TOKEN_INVALIDO",
                        "El código es inválido."));
        if (!token.vigente(ahora)) {
            throw new BusinessException.Gone("TOKEN_EXPIRADO",
                    "El código ya fue usado o expiró.");
        }
        try {
            Usuario.validarPassword(nuevaPassword);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException.BadRequest("PASSWORD_INVALIDA", ex.getMessage());
        }
        Usuario usuario = usuarioRepository.porId(token.usuarioId())
                .orElseThrow(() -> new BusinessException.NotFound("USUARIO_NO_ENCONTRADO", "Usuario no encontrado."));
        tokenRecuperacionRepository.guardar(new TokenRecuperacion(token.id(), token.usuarioId(), token.codigoHash(),
                token.fechaCreacion(), token.fechaExpiracion(), true));
        usuarioRepository.guardar(new Usuario(usuario.id(), usuario.email(), passwordEncoder.encode(nuevaPassword),
                usuario.rol(), usuario.estadoCuenta(), usuario.fechaCreacion()));
        sesionPort.cerrarSesionesDeUsuario(usuario.id());
    }

    /** Creación de administrador por bootstrap (variables de entorno). No es un endpoint. */
    @Transactional
    public void crearAdministrador(String email, String password) {
        String correo = Usuario.normalizarEmail(email);
        if (usuarioRepository.existeEmail(correo)) {
            throw new BusinessException.Conflict("CORREO_YA_REGISTRADO",
                    "La cuenta administradora ya existe.");
        }
        try {
            Usuario.validarPassword(password);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException.BadRequest("PASSWORD_INVALIDA", ex.getMessage());
        }
        OffsetDateTime ahora = OffsetDateTime.now();
        Usuario admin = new Usuario(UUID.randomUUID(), correo, passwordEncoder.encode(password),
                Rol.ADMINISTRADOR, EstadoCuenta.ACTIVA, ahora);
        usuarioRepository.guardar(admin);
        consentimientoRepository.guardar(new Consentimiento(UUID.randomUUID(), admin.id(), "admin-bootstrap", ahora));
        perfilBasePort.crearPerfilBase(admin.id());
    }

    @Override
    @Transactional
    public void bloquearCuenta(UUID usuarioId) {
        Usuario usuario = usuarioRepository.porId(usuarioId)
                .orElseThrow(() -> new BusinessException.NotFound("USUARIO_NO_ENCONTRADO", "Usuario no encontrado."));
        usuarioRepository.guardar(new Usuario(usuario.id(), usuario.email(), usuario.passwordHash(), usuario.rol(),
                EstadoCuenta.BLOQUEADA_PREVENTIVAMENTE, usuario.fechaCreacion()));
        sesionPort.cerrarSesionesDeUsuario(usuarioId);
    }

    @Override
    @Transactional
    public void desbloquearCuenta(UUID usuarioId) {
        Usuario usuario = usuarioRepository.porId(usuarioId)
                .orElseThrow(() -> new BusinessException.NotFound("USUARIO_NO_ENCONTRADO", "Usuario no encontrado."));
        usuarioRepository.guardar(new Usuario(usuario.id(), usuario.email(), usuario.passwordHash(), usuario.rol(),
                EstadoCuenta.ACTIVA, usuario.fechaCreacion()));
    }

    @Override
    public Optional<Usuario> porId(UUID id) {
        return usuarioRepository.porId(id);
    }

    @Override
    public boolean existe(UUID id) {
        return usuarioRepository.porId(id).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> buscarPorTexto(String texto) {
        return usuarioRepository.buscarPorTexto(texto);
    }

    @Override
    public boolean esPrestador(UUID usuarioId) {
        return usuarioRepository.porId(usuarioId).map(u -> u.rol() == Rol.PRESTADOR).orElse(false);
    }
}
