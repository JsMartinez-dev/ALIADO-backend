package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port;

import java.util.UUID;

/** Erasure transaction for the approved D-05 MVP policy. */
public interface EliminacionDefinitivaPort {
    boolean estaEliminada(UUID usuarioId);
    void anonimizarYEncolarArchivos(UUID usuarioId, UUID operacionId);
}
