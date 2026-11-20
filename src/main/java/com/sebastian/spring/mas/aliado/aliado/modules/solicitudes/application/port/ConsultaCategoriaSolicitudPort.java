package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port;

import java.util.UUID;

public interface ConsultaCategoriaSolicitudPort {
    boolean estaActiva(UUID categoriaId);
}
