package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port;

import java.util.UUID;

/** Consulta de catálogo requerida por solicitudes, sin detalles de persistencia. */
public interface ConsultaZonaSolicitudPort {
    boolean estaActiva(UUID zonaId);
}
