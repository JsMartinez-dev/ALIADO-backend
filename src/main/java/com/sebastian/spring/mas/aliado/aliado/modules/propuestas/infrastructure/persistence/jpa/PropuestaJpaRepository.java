package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.infrastructure.persistence.jpa;

import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.infrastructure.persistence.entity.PropuestaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropuestaJpaRepository extends JpaRepository<PropuestaEntity, UUID> {
    Optional<PropuestaEntity> findBySolicitudIdAndPrestadorIdAndEstado(UUID solicitudId, UUID prestadorId, String estado);
    List<PropuestaEntity> findBySolicitudIdAndEstado(UUID solicitudId, String estado);
    List<PropuestaEntity> findBySolicitudId(UUID solicitudId);
    List<PropuestaEntity> findByPrestadorIdOrderByFechaCreacionDescIdDesc(UUID prestadorId, Pageable pagina);
}
