package com.sebastian.spring.mas.aliado.aliado.modules.administracion.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.presentation.dto.AdminDtos;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.presentation.mapper.AdminPresentationMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminReportesController {
  private final ConsultarColaReportesUseCase consultarColaReportesUseCase;
  private final RevisarReporteUseCase revisarReporteUseCase;
  private final ResolverReporteUseCase resolverReporteUseCase;
  private final DescargarEvidenciaAdministrativaUseCase descargarEvidenciaAdministrativaUseCase;

  public AdminReportesController(
      ConsultarColaReportesUseCase consultarColaReportesUseCase,
      RevisarReporteUseCase revisarReporteUseCase,
      ResolverReporteUseCase resolverReporteUseCase,
      DescargarEvidenciaAdministrativaUseCase descargarEvidenciaAdministrativaUseCase) {
    this.consultarColaReportesUseCase = consultarColaReportesUseCase;
    this.revisarReporteUseCase = revisarReporteUseCase;
    this.resolverReporteUseCase = resolverReporteUseCase;
    this.descargarEvidenciaAdministrativaUseCase = descargarEvidenciaAdministrativaUseCase;
  }

  @GetMapping("/reportes")
  public ResponseEntity<List<AdminDtos.ReporteResponse>> colaReportes(
      @RequestParam(defaultValue = "PENDIENTE") String estado) {
    return ResponseEntity.ok(
        consultarColaReportesUseCase.ejecutar(new ConsultarColaReportesCommand(estado)).stream()
            .map(AdminPresentationMapper::aResponse)
            .toList());
  }

  @GetMapping("/reportes/{id}")
  public ResponseEntity<AdminDtos.ReporteResponse> detalleReporte(@PathVariable UUID id) {
    var reporte =
        revisarReporteUseCase.ejecutar(new RevisarReporteCommand(id, SecurityUtils.actual().id()));
    return ResponseEntity.ok(AdminPresentationMapper.aResponse(reporte));
  }

  @GetMapping("/reportes/{id}/evidencias/{evidenciaId}")
  public ResponseEntity<byte[]> descargarEvidencia(
      @PathVariable UUID id, @PathVariable UUID evidenciaId) {
    var contenido =
        descargarEvidenciaAdministrativaUseCase.ejecutar(
            new DescargarEvidenciaAdministrativaCommand(id, evidenciaId));
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contenido.tipoContenido()))
        .body(contenido.contenido());
  }

  @PostMapping("/reportes/{id}/resolver")
  public ResponseEntity<AdminDtos.ReporteResponse> resolverReporte(
      @PathVariable UUID id, @Valid @RequestBody AdminDtos.ResolverReporteRequest req) {
    return ResponseEntity.ok(
        AdminPresentationMapper.aResponse(
            resolverReporteUseCase.ejecutar(
                new ResolverReporteCommand(
                    id, SecurityUtils.actual().id(), req.resultado(), req.justificacion()))));
  }
}
