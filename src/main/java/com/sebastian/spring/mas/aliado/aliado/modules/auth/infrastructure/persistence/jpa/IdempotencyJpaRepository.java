package com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.persistence.jpa;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.persistence.entity.IdempotencyKeyEntity;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.persistence.entity.IdempotencyKeyId;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IdempotencyJpaRepository extends JpaRepository<IdempotencyKeyEntity, IdempotencyKeyId> {
    @Query(value = "select pg_advisory_xact_lock(hashtext(:operacion), hashtext(cast(:clave as text)))", nativeQuery = true)
    void bloquear(@Param("clave") UUID clave, @Param("operacion") String operacion);
}
