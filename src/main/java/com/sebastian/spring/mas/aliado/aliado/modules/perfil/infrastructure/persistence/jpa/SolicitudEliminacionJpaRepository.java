package com.sebastian.spring.mas.aliado.aliado.modules.perfil.infrastructure.persistence.jpa;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.infrastructure.persistence.entity.SolicitudEliminacionEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

public interface SolicitudEliminacionJpaRepository extends JpaRepository<SolicitudEliminacionEntity, UUID> {
    Optional<SolicitudEliminacionEntity> findByUsuarioIdAndEstado(UUID usuarioId, String estado);
    Optional<SolicitudEliminacionEntity> findFirstByUsuarioIdOrderByFechaSolicitudDesc(UUID usuarioId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update SolicitudEliminacionEntity s set s.estado = :nuevo, s.fechaConfirmacion = :confirmacion"
            + " where s.id = :id and s.estado = :esperado")
    int transicionar(UUID id, String esperado, String nuevo, OffsetDateTime confirmacion);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update SolicitudEliminacionEntity s set s.estado = 'EXPIRADA'"
            + " where s.estado = 'PENDIENTE' and s.fechaExpiracion < :ahora")
    int expirarVencidas(OffsetDateTime ahora);

    @Query("select s from SolicitudEliminacionEntity s where s.estado = 'PENDIENTE' and s.fechaExpiracion < :ahora")
    List<SolicitudEliminacionEntity> findPendientesVencidas(OffsetDateTime ahora);
}
