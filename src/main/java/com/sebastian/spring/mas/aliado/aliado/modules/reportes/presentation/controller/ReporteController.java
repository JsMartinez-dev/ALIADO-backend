package com.sebastian.spring.mas.aliado.aliado.modules.reportes.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.presentation.mapper.ReportePresentationMapper;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.presentation.dto.ReporteDtos;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class ReporteController {

    private final ReportarUsuarioUseCase usuarioUseCase;
    private final ReportarServicioUseCase servicioUseCase;
    private final AdjuntarEvidenciaReporteUseCase evidenciaUseCase;
    private final ConsultarReportePropioUseCase consultaUseCase;
    private final DescargarEvidenciaPropiaUseCase descargaUseCase;
    public ReporteController(ReportarUsuarioUseCase usuarioUseCase, ReportarServicioUseCase servicioUseCase, AdjuntarEvidenciaReporteUseCase evidenciaUseCase, ConsultarReportePropioUseCase consultaUseCase, DescargarEvidenciaPropiaUseCase descargaUseCase) {
        this.usuarioUseCase = usuarioUseCase;
        this.servicioUseCase = servicioUseCase;
        this.evidenciaUseCase = evidenciaUseCase;
        this.consultaUseCase = consultaUseCase;
        this.descargaUseCase = descargaUseCase;
    }

    @PostMapping("/usuarios/{id}/reportes")
    public ResponseEntity<ReporteDtos.ReporteIdResponse> reportarUsuario(@PathVariable UUID id,
            @Valid @RequestBody ReporteDtos.CrearRequest req) {
        UUID reporteId = usuarioUseCase.ejecutar(ReportePresentationMapper.aUsuarioCommand(id, SecurityUtils.actual().id(), req));
        return ResponseEntity.status(HttpStatus.CREATED).body(new ReporteDtos.ReporteIdResponse(reporteId));
    }

    @PostMapping("/servicios/{id}/reportes")
    public ResponseEntity<ReporteDtos.ReporteIdResponse> reportarServicio(@PathVariable UUID id,
            @Valid @RequestBody ReporteDtos.CrearRequest req) {
        UUID reporteId = servicioUseCase.ejecutar(ReportePresentationMapper.aServicioCommand(id, SecurityUtils.actual().id(), req));
        return ResponseEntity.status(HttpStatus.CREATED).body(new ReporteDtos.ReporteIdResponse(reporteId));
    }

    @PostMapping(value = "/reportes/{id}/evidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReporteDtos.EvidenciaResponse> agregarEvidencia(@PathVariable UUID id,
            @RequestPart("archivo") MultipartFile archivo) {
        byte[] contenido;
        try {
            contenido = archivo.getBytes();
        } catch (IOException ex) {
            throw new BusinessException.BadRequest("ARCHIVO_INVALIDO", "No se pudo leer el archivo enviado.");
        }
        var evidencia = evidenciaUseCase.ejecutar(new AdjuntarEvidenciaReporteCommand(id, SecurityUtils.actual().id(), archivo.getOriginalFilename(), archivo.getContentType(), contenido));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ReporteDtos.EvidenciaResponse(evidencia.id(), evidencia.archivoKey(), evidencia.tipoContenido()));
    }

    @GetMapping("/reportes/{id}/evidencias/{evidenciaId}")
    public ResponseEntity<byte[]> descargarEvidencia(@PathVariable UUID id, @PathVariable UUID evidenciaId) {
        var contenido = descargaUseCase.ejecutar(new DescargarEvidenciaPropiaCommand(id, evidenciaId, SecurityUtils.actual().id()));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contenido.tipoContenido()))
                .body(contenido.contenido());
    }

    @GetMapping("/reportes/mios/{id}")
    public ResponseEntity<ReporteDtos.ReporteResponse> miReporte(@PathVariable UUID id) {
        var reporte = consultaUseCase.ejecutar(new ConsultarReportePropioCommand(id, SecurityUtils.actual().id()));
        return ResponseEntity.ok(ReportePresentationMapper.aResponse(reporte));
    }
}
