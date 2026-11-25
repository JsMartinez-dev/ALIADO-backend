package com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port;

import java.util.UUID;

public interface PerfilBasePort {
    void crearPerfilBase(UUID usuarioId);
}
