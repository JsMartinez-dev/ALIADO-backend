package com.sebastian.spring.mas.aliado.aliado.modules.reportes.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.common.web.BaseController;
import com.sebastian.spring.mas.aliado.aliado.common.idempotencia.IdempotenciaPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.presentation.mapper.ReportePresentationMapper;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.presentation.dto.ReporteDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Reportes y evidencias (SPEC-11). */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Reportes", description = "SPEC-11. Reportes, evidencias y consultas del reportante.")
public class ReporteController extends BaseController {

    private final ReportarUsuarioUseCase usuarioUseCase;
    private final ReportarServicioUseCase servicioUseCase;
    private final AdjuntarEvidenciaReporteUseCase evidenciaUseCase;
    private final ConsultarReportePropioUseCase consultaUseCase;
    private final DescargarEvidenciaPropiaUseCase descargaUseCase;
    private final ConsultarReportesPropiosUseCase reportesPropiosUseCase;
    public ReporteController(ReportarUsuarioUseCase usuarioUseCase, ReportarServicioUseCase servicioUseCase, AdjuntarEvidenciaReporteUseCase evidenciaUseCase, ConsultarReportePropioUseCase consultaUseCase, DescargarEvidenciaPropiaUseCase descargaUseCase, ConsultarReportesPropiosUseCase reportesPropiosUseCase, ObjectMapper objectMapper, IdempotenciaPort idempotencyService) {
        super(objectMapper, idempotencyService);
        this.usuarioUseCase = usuarioUseCase;
        this.servicioUseCase = servicioUseCase;
        this.evidenciaUseCase = evidenciaUseCase;
        this.consultaUseCase = consultaUseCase;
        this.descargaUseCase = descargaUseCase;
        this.reportesPropiosUseCase = reportesPropiosUseCase;
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
    @Operation(summary = "Adjuntar evidencia a un reporte propio abierto",
            description = "Acepta Idempotency-Key opcional para reintentos sin duplicar la evidencia.")
    public ResponseEntity<?> agregarEvidencia(@PathVariable UUID id,
            @RequestPart("archivo") MultipartFile archivo,
            @RequestHeader(value = "Idempotency-Key", required = false) String claveHeader) {
        return ejecutarIdempotente(claveHeader, "reportes.evidencia:" + id, r -> SecurityUtils.actual().id(),
                HttpStatus.CREATED.value(), () -> {
                    byte[] contenido;
                    try {
                        contenido = archivo.getBytes();
                    } catch (IOException ex) {
                        throw new BusinessException.BadRequest("ARCHIVO_INVALIDO",
                                "No se pudo leer el archivo enviado.");
                    }
                    var evidencia = evidenciaUseCase.ejecutar(new AdjuntarEvidenciaReporteCommand(
                            id, SecurityUtils.actual().id(), archivo.getOriginalFilename(),
                            archivo.getContentType(), contenido));
                    return new ReporteDtos.EvidenciaResponse(evidencia.id(), evidencia.archivoKey(),
                            evidencia.tipoContenido());
                });
    }

    @GetMapping("/reportes/{id}/evidencias/{evidenciaId}")
    public ResponseEntity<byte[]> descargarEvidencia(@PathVariable UUID id, @PathVariable UUID evidenciaId) {
        var contenido = descargaUseCase.ejecutar(new DescargarEvidenciaPropiaCommand(id, evidenciaId, SecurityUtils.actual().id()));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contenido.tipoContenido()))
                .body(contenido.contenido());
    }

    @GetMapping("/reportes/mios")
    @Operation(summary = "Listar reportes propios",
            description = "Requiere sesión. Aísla por reportante y ordena por fechaCreacion descendente.")
    public ResponseEntity<ReporteDtos.ReportesPropiosPage> misReportes(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int pagina,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int tamano) {
        if (pagina < 0 || tamano < 1 || tamano > 100) {
            throw new BusinessException.BadRequest("PAGINACION_INVALIDA",
                    "pagina debe ser >= 0 y tamano debe estar entre 1 y 100.");
        }
        var result = reportesPropiosUseCase.ejecutar(SecurityUtils.actual().id(), pagina, tamano);
        return ResponseEntity.ok(new ReporteDtos.ReportesPropiosPage(
                result.items().stream().map(ReportePresentationMapper::aResponse).toList(),
                result.pagina(), result.tamano(), result.totalElementos(), result.totalPaginas()));
    }

    @GetMapping("/reportes/mios/{id}")
    public ResponseEntity<ReporteDtos.ReporteResponse> miReporte(@PathVariable UUID id) {
        var reporte = consultaUseCase.ejecutar(new ConsultarReportePropioCommand(id, SecurityUtils.actual().id()));
        return ResponseEntity.ok(ReportePresentationMapper.aResponse(reporte));
    }
}
