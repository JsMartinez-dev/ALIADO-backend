package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port;

import java.util.UUID;

/** Datos de catálogo que requiere el perfil profesional. */
public interface ConsultaCatalogoPerfilPort {
    boolean categoriaActiva(UUID id);
    boolean zonaActiva(UUID id);
}
