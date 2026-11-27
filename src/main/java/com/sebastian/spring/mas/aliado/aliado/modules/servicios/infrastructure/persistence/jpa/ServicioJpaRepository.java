package com.sebastian.spring.mas.aliado.aliado.modules.servicios.infrastructure.persistence.jpa;

import com.sebastian.spring.mas.aliado.aliado.modules.servicios.infrastructure.persistence.entity.ServicioEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ServicioJpaRepository extends JpaRepository<ServicioEntity, UUID> {
    Optional<ServicioEntity> findBySolicitudId(UUID solicitudId);

    @Query("select s from ServicioEntity s where s.clienteId = :usuarioId or s.prestadorId = :usuarioId order by s.fechaContratacion desc, s.id asc")
    List<ServicioEntity> findByUsuario(UUID usuarioId);

    @Query("select s from ServicioEntity s where s.clienteId = :usuarioId or s.prestadorId = :usuarioId order by s.fechaContratacion desc, s.id asc")
    List<ServicioEntity> findByUsuario(UUID usuarioId, Pageable pageable);

    @Query("select count(s) from ServicioEntity s where s.clienteId = :usuarioId or s.prestadorId = :usuarioId")
    long countByUsuario(UUID usuarioId);

    @Query("select s from ServicioEntity s where (s.clienteId = :usuarioId or s.prestadorId = :usuarioId)"
            + " and s.estado in ('CONTRATADO', 'EN_EJECUCION', 'TERMINADO')")
    List<ServicioEntity> findActivosDeUsuario(UUID usuarioId);
}
