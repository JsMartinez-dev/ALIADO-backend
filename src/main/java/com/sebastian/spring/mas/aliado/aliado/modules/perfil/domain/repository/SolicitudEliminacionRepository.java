package com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.SolicitudEliminacion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.EstadoEliminacion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SolicitudEliminacionRepository {
    Optional<SolicitudEliminacion> pendienteDeUsuario(UUID usuarioId);
    List<SolicitudEliminacion> pendientesVencidas(java.time.OffsetDateTime ahora);
    SolicitudEliminacion guardar(SolicitudEliminacion solicitud);
    Optional<SolicitudEliminacion> ultimaDeUsuario(UUID usuarioId);
    boolean transicionar(UUID id, EstadoEliminacion esperado, EstadoEliminacion nuevo,
                         java.time.OffsetDateTime fechaConfirmacion);
    void expirarVencidas(java.time.OffsetDateTime ahora);
}
