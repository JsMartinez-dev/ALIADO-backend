package com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.Sesion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SesionRepository {
    Optional<Sesion> porTokenHash(String tokenHash);
    List<Sesion> sesionesActivasDeUsuario(UUID usuarioId);
    Sesion guardar(Sesion sesion);
}
