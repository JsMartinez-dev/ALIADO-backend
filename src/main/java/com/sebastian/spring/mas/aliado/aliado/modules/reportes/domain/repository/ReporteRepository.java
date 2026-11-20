package com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.repository;

import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.model.Reporte;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReporteRepository {
    Optional<Reporte> porId(UUID id);
    /** Serializa las mutaciones de evidencia y resolución sobre el reporte. */
    Optional<Reporte> porIdParaActualizar(UUID id);
    Optional<Reporte> abiertoEquivalente(UUID reportanteId, String tipo, UUID objetivoId, String motivo);
    List<Reporte> cola(String estado);
    List<Reporte> porReportante(UUID reportanteId);
    Reporte guardar(Reporte reporte);
}
