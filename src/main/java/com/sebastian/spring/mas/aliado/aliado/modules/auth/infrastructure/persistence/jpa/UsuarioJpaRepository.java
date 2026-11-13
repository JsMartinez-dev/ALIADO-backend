package com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.persistence.jpa;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.persistence.entity.UsuarioEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {
    @Query("select u from UsuarioEntity u where lower(u.email) = lower(:email)")
    Optional<UsuarioEntity> findByEmailIgnoreCase(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UsuarioEntity u where lower(u.email) = lower(:email)")
    Optional<UsuarioEntity> findByEmailParaIniciarSesion(String email);

    @Query("select count(u) > 0 from UsuarioEntity u where lower(u.email) = lower(:email)")
    boolean existsByEmailIgnoreCase(String email);

    @Query("select u from UsuarioEntity u where lower(u.email) like lower(concat('%', :texto, '%'))")
    List<UsuarioEntity> buscarPorEmail(String texto);
}
