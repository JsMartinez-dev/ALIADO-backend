package com.sebastian.spring.mas.aliado.aliado.modules.reportes.infrastructure.persistence.jpa;

import com.sebastian.spring.mas.aliado.aliado.modules.reportes.infrastructure.persistence.entity.ReporteEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface ReporteJpaRepository extends JpaRepository<ReporteEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReporteEntity r where r.id = :id")
    Optional<ReporteEntity> findByIdForUpdate(UUID id);
    List<ReporteEntity> findByEstadoOrderByFechaCreacionAsc(String estado);

    List<ReporteEntity> findByReportanteIdOrderByFechaCreacionDesc(UUID reportanteId);

    @Query("select r from ReporteEntity r where r.reportanteId = :reportanteId and r.tipo = :tipo"
            + " and r.motivo = :motivo and r.estado in ('PENDIENTE', 'EN_REVISION')")
    List<ReporteEntity> findAbiertosEquivalentes(UUID reportanteId, String tipo, String motivo);
}
