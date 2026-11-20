package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port;

import java.util.UUID;

/** Consulta requerida por perfil para impedir eliminación durante la ejecución de un servicio. */
public interface ConsultaServiciosActivosPort {
    boolean bloqueaEliminacion(UUID usuarioId);
}
