package com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.persistence.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.EstadoSesion;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.Sesion;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.SesionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.persistence.jpa.SesionJpaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.persistence.mapper.AuthPersistenceMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SesionPersistenceAdapter implements SesionRepository {
    private final SesionJpaRepository jpa;

    public SesionPersistenceAdapter(SesionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Sesion> porTokenHash(String tokenHash) {
        return jpa.findByTokenHash(tokenHash).map(AuthPersistenceMapper::aDominio);
    }

    @Override
    public List<Sesion> sesionesActivasDeUsuario(UUID usuarioId) {
        return jpa.findByUsuarioIdAndEstado(usuarioId, EstadoSesion.ACTIVA.name()).stream()
                .map(AuthPersistenceMapper::aDominio).toList();
    }

    @Override
    public Sesion guardar(Sesion sesion) {
        return AuthPersistenceMapper.aDominio(jpa.save(AuthPersistenceMapper.aEntidad(sesion)));
    }
}
