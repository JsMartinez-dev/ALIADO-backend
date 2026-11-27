package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port;

import java.util.UUID;

/** Projection needed to replace deleted identities with "Cuenta eliminada". */
public interface CuentaEliminadaPort {
    boolean eliminada(UUID usuarioId);
}
