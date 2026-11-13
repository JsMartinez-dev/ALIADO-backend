package com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.Usuario;
import java.util.Optional;
import java.util.UUID;

/** Contrato público de sesiones opacas (SPEC-01): token aleatorio, en BD solo su hash. */
public interface SesionPort {
    Optional<Usuario> validar(String token);
    boolean cerrarSesion(String token);
    void cerrarSesionesDeUsuario(UUID usuarioId);
}
