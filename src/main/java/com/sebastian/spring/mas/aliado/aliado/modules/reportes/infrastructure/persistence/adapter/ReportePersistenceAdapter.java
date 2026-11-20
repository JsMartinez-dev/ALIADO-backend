package com.sebastian.spring.mas.aliado.aliado.modules.reportes.infrastructure.persistence.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.model.EvidenciaReporte;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.model.Reporte;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.repository.EvidenciaReporteRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.repository.ReporteRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.infrastructure.persistence.jpa.EvidenciaReporteJpaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.infrastructure.persistence.jpa.ReporteJpaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.infrastructure.persistence.mapper.ReportePersistenceMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ReportePersistenceAdapter implements ReporteRepository, EvidenciaReporteRepository {
    private final ReporteJpaRepository reportes;
    private final EvidenciaReporteJpaRepository evidencias;

    public ReportePersistenceAdapter(ReporteJpaRepository reportes, EvidenciaReporteJpaRepository evidencias) {
        this.reportes = reportes;
        this.evidencias = evidencias;
    }

    @Override
    public Optional<Reporte> porId(UUID id) {
        return reportes.findById(id).map(ReportePersistenceMapper::aDominio);
    }

    @Override
    public Optional<Reporte> porIdParaActualizar(UUID id) {
        return reportes.findByIdForUpdate(id).map(ReportePersistenceMapper::aDominio);
    }

    @Override
    public Optional<Reporte> abiertoEquivalente(UUID reportanteId, String tipo, UUID objetivoId, String motivo) {
        return reportes.findAbiertosEquivalentes(reportanteId, tipo, motivo).stream()
                .map(ReportePersistenceMapper::aDominio)
                .filter(r -> objetivoId.equals(r.objetivoId()))
                .findFirst();
    }

    @Override
    public List<Reporte> cola(String estado) {
        return reportes.findByEstadoOrderByFechaCreacionAsc(estado).stream()
                .map(ReportePersistenceMapper::aDominio).toList();
    }

    @Override
    public List<Reporte> porReportante(UUID reportanteId) {
        return reportes.findByReportanteIdOrderByFechaCreacionDesc(reportanteId).stream()
                .map(ReportePersistenceMapper::aDominio).toList();
    }

    @Override
    public Reporte guardar(Reporte reporte) {
        return ReportePersistenceMapper.aDominio(reportes.save(ReportePersistenceMapper.aEntidad(reporte)));
    }

    @Override
    public List<EvidenciaReporte> porReporte(UUID reporteId) {
        return evidencias.findByReporteId(reporteId).stream().map(ReportePersistenceMapper::aDominio).toList();
    }

    @Override
    public EvidenciaReporte guardar(EvidenciaReporte evidencia) {
        return ReportePersistenceMapper.aDominio(evidencias.save(ReportePersistenceMapper.aEntidad(evidencia)));
    }
}
