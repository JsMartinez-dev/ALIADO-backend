package com.sebastian.spring.mas.aliado.aliado.modules.confianza.infrastructure.persistence.jpa;

import com.sebastian.spring.mas.aliado.aliado.modules.confianza.infrastructure.persistence.entity.CalificacionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalificacionJpaRepository extends JpaRepository<CalificacionEntity, UUID> {
    Optional<CalificacionEntity> findByServicioId(UUID servicioId);
    List<CalificacionEntity> findByPrestadorId(UUID prestadorId);
    List<CalificacionEntity> findByClienteIdOrderByFechaDesc(UUID clienteId);
}
