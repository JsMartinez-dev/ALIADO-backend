package com.sebastian.spring.mas.aliado.aliado.modules.auth.application;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.SesionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.EstadoSesion;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.Sesion;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.Usuario;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.SesionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sesiones opacas (SPEC-01, plan general 6.2): token aleatorio seguro, en BD solo su hash SHA-256.
 * Decisión documentada (la política de sesiones simultáneas no estaba definida):
 * al iniciar sesión se cierran las sesiones activas anteriores del usuario.
 */
@Service
public class SesionService implements SesionPort {
    private final SesionRepository sesiones;
    private final UsuarioRepository usuarios;

    public SesionService(SesionRepository sesiones, UsuarioRepository usuarios) {
        this.sesiones = sesiones;
        this.usuarios = usuarios;
    }

    public static String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 no disponible.", ex);
        }
    }

    /** Valida un token opaco: sesión activa, no vencida y cuenta no bloqueada. */
    @Override
    @Transactional
    public Optional<Usuario> validar(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        OffsetDateTime ahora = OffsetDateTime.now();
        return sesiones.porTokenHash(hashToken(token))
                .flatMap(sesion -> {
                    if (!sesion.vigente(ahora)) {
                        if (sesion.estado() == EstadoSesion.ACTIVA) {
                            sesiones.guardar(sesion.expirada());
                        }
                        return Optional.<Sesion>empty();
                    }
                    return Optional.of(sesion);
                })
                .flatMap(sesion -> usuarios.porId(sesion.usuarioId()))
                .filter(usuario -> !usuario.bloqueado());
    }

    @Override
    @Transactional
    public boolean cerrarSesion(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return sesiones.porTokenHash(hashToken(token))
                .filter(sesion -> sesion.estado() == EstadoSesion.ACTIVA)
                .map(sesion -> {
                    sesiones.guardar(sesion.cerrada(OffsetDateTime.now()));
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public void cerrarSesionesDeUsuario(UUID usuarioId) {
        OffsetDateTime ahora = OffsetDateTime.now();
        for (Sesion sesion : sesiones.sesionesActivasDeUsuario(usuarioId)) {
            sesiones.guardar(sesion.cerrada(ahora));
        }
    }
}
