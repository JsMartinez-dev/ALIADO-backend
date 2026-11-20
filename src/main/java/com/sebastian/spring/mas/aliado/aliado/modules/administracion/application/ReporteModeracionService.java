package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application;

import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.EvidenciaContenido;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.port.ReporteAdminPort;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.model.EvidenciaReporte;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.model.Reporte;
import java.util.List;
import java.util.UUID;

/**
 * Cola de reportes y su resolución (SPEC-12, UC068–UC070). Resolver exige haber revisado el detalle
 * y justificar (SPEC-12).
 */
public class ReporteModeracionService {
  private final ReporteAdminPort reportes;
  private final com.sebastian.spring.mas.aliado.aliado.common.auditoria.AuditoriaPort
      auditoriaService;

  public ReporteModeracionService(
      ReporteAdminPort reportes,
      com.sebastian.spring.mas.aliado.aliado.common.auditoria.AuditoriaPort auditoriaService) {
    this.reportes = reportes;
    this.auditoriaService = auditoriaService;
  }

  public List<Reporte> colaReportes(String estado) {
    return reportes.cola(estado == null ? "PENDIENTE" : estado);
  }

  public Reporte detalleReporte(UUID reporteId, UUID administradorId) {
    Reporte reporte = reportes.marcarRevisionDetalle(reporteId, administradorId);
    auditoriaService.registrar(administradorId, "REPORT_REVIEWED", "REPORTE", reporteId, null);
    return reporte;
  }

  public Reporte resolverReporte(
      UUID reporteId, UUID administradorId, String resultado, String justificacion) {
    Reporte resuelto = reportes.resolver(reporteId, administradorId, resultado, justificacion);
    auditoriaService.registrar(administradorId, "REPORT_RESOLVED", "REPORTE", reporteId, resultado);
    return resuelto;
  }

  public List<EvidenciaReporte> evidenciasDe(UUID reporteId) {
    return reportes.evidenciasDe(reporteId);
  }

  public EvidenciaContenido descargarEvidencia(UUID reporteId, UUID evidenciaId) {
    return reportes.descargarEvidencia(reporteId, evidenciaId);
  }
}
