package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port;

import java.util.Optional;
import java.util.UUID;

/** Lectura interna del nombre para proyecciones que ya autorizaron el acceso. */
public interface NombreVisiblePerfilPort {
    Optional<String> nombreVisibleDe(UUID usuarioId);
}
