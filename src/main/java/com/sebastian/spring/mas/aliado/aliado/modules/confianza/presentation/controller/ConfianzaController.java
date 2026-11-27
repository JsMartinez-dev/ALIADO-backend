package com.sebastian.spring.mas.aliado.aliado.modules.confianza.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.presentation.mapper.ConfianzaPresentationMapper;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.presentation.dto.ConfianzaDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

/** Calificaciones, reseñas, reputación y verificación (SPEC-10). */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Confianza", description = "SPEC-10. Calificaciones, reseñas y verificación.")
public class ConfianzaController {

    private final CalificarServicioUseCase calificarUseCase;
    private final AgregarResenaUseCase resenaUseCase;
    private final ConsultarConfianzaUseCase confianzaUseCase;
    private final ConsultarResenasPublicasUseCase resenasUseCase;
    private final SolicitarVerificacionUseCase verificacionUseCase;
    private final ConsultarVerificacionPropiaUseCase miVerificacionUseCase;
    private final ReportarResenaUseCase reporteUseCase;
    private final ConsultarCalificacionPropiaUseCase calificacionPropiaUseCase;
    private final ConsultarCalificacionesPropiasUseCase calificacionesPropiasUseCase;
    public ConfianzaController(CalificarServicioUseCase calificarUseCase, AgregarResenaUseCase resenaUseCase, ConsultarConfianzaUseCase confianzaUseCase, ConsultarResenasPublicasUseCase resenasUseCase, SolicitarVerificacionUseCase verificacionUseCase, ConsultarVerificacionPropiaUseCase miVerificacionUseCase, ReportarResenaUseCase reporteUseCase, ConsultarCalificacionPropiaUseCase calificacionPropiaUseCase, ConsultarCalificacionesPropiasUseCase calificacionesPropiasUseCase) {
        this.calificarUseCase = calificarUseCase;
        this.resenaUseCase = resenaUseCase;
        this.confianzaUseCase = confianzaUseCase;
        this.resenasUseCase = resenasUseCase;
        this.verificacionUseCase = verificacionUseCase;
        this.miVerificacionUseCase = miVerificacionUseCase;
        this.reporteUseCase = reporteUseCase;
        this.calificacionPropiaUseCase = calificacionPropiaUseCase;
        this.calificacionesPropiasUseCase = calificacionesPropiasUseCase;
    }

    @PostMapping("/servicios/{servicioId}/calificacion")
    public ResponseEntity<ConfianzaDtos.CalificacionResponse> calificar(@PathVariable UUID servicioId,
            @Valid @RequestBody ConfianzaDtos.CalificarRequest req) {
        var calificacion = calificarUseCase.ejecutar(ConfianzaPresentationMapper.aCommand(servicioId, SecurityUtils.actual().id(), req));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ConfianzaPresentationMapper.aResponse(calificacion));
    }

    @GetMapping("/calificaciones/mias")
    @Operation(summary = "Historial de calificaciones propias",
            description = "Requiere rol CLIENTE. Incluye reseña cuando existe. Orden descendente por fecha.")
    public ResponseEntity<ConfianzaDtos.CalificacionesPropiasPage> misCalificaciones(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int pagina,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int tamano) {
        SecurityUtils.exigirRol("CLIENTE");
        if (pagina < 0 || tamano < 1 || tamano > 100) {
            throw new com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException.BadRequest(
                    "PAGINACION_INVALIDA", "pagina debe ser >= 0 y tamano debe estar entre 1 y 100.");
        }
        var result = calificacionesPropiasUseCase.ejecutar(SecurityUtils.actual().id(), pagina, tamano);
        return ResponseEntity.ok(new ConfianzaDtos.CalificacionesPropiasPage(
                result.items().stream().map(i -> new ConfianzaDtos.CalificacionHistorialItem(
                        i.calificacionId(), i.servicioId(), i.valor(), i.fechaCalificacion(),
                        i.resena() == null ? null : new ConfianzaDtos.ResenaHistorial(
                                i.resena().id(), i.resena().texto(), i.resena().fechaCreacion()))).toList(),
                result.pagina(), result.tamano(), result.totalElementos(), result.totalPaginas()));
    }

    @GetMapping("/servicios/{servicioId}/calificacion")
    @Operation(summary = "Consultar calificación propia de un servicio",
            description = "Requiere rol CLIENTE y ser propietario del servicio.")
    @ApiResponse(responseCode = "404", description = "Servicio sin calificación o inexistente")
    public ResponseEntity<ConfianzaDtos.CalificacionPropiaResponse> miCalificacion(@PathVariable UUID servicioId) {
        SecurityUtils.exigirRol("CLIENTE");
        var resultado = calificacionPropiaUseCase.ejecutar(new ConsultarCalificacionPropiaCommand(
                servicioId, SecurityUtils.actual().id()));
        return ResponseEntity.ok(new ConfianzaDtos.CalificacionPropiaResponse(
                resultado.calificacionId(), resultado.valor(), resultado.tieneResena()));
    }

    @PostMapping("/calificaciones/{id}/resena")
    public ResponseEntity<ConfianzaDtos.ResenaResponse> resena(@PathVariable UUID id,
            @Valid @RequestBody ConfianzaDtos.ResenaRequest req) {
        var resena = resenaUseCase.ejecutar(ConfianzaPresentationMapper.aCommand(id, SecurityUtils.actual().id(), req));
        return ResponseEntity.status(HttpStatus.CREATED).body(new ConfianzaDtos.ResenaResponse(resena));
    }

    @GetMapping("/prestadores/{prestadorId}/confianza")
    public ResponseEntity<ConfianzaDtos.ConfianzaResponse> confianza(@PathVariable UUID prestadorId) {
        var info = confianzaUseCase.ejecutar(new ConsultarConfianzaCommand(prestadorId));
        return ResponseEntity.ok(ConfianzaPresentationMapper.aResponse(info));
    }

    @GetMapping("/prestadores/{prestadorId}/resenas")
    public ResponseEntity<List<ConfianzaDtos.ResenaPublicaResponse>> resenas(@PathVariable UUID prestadorId) {
        return ResponseEntity.ok(resenasUseCase.ejecutar(new ConsultarResenasPublicasCommand(prestadorId)).stream()
                .map(r -> new ConfianzaDtos.ResenaPublicaResponse(r.id(), r.valor(), r.texto(),
                        r.fechaCreacion()))
                .toList());
    }

    @PostMapping(value = "/verificaciones", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ConfianzaDtos.VerificacionResponse> solicitarVerificacion(
            @RequestPart("documentos") List<MultipartFile> documentos) {
        SecurityUtils.exigirRol("PRESTADOR");
        List<ArchivoVerificacion> archivos = new ArrayList<>();
        for (MultipartFile archivo : documentos) {
            byte[] contenido;
            try {
                contenido = archivo.getBytes();
            } catch (IOException ex) {
                throw new BusinessException.BadRequest("ARCHIVO_INVALIDO",
                        "No se pudo leer el archivo enviado.");
            }
            archivos.add(new ArchivoVerificacion(archivo.getOriginalFilename(),
                    archivo.getContentType(), contenido));
        }
        var solicitud = verificacionUseCase.ejecutar(new SolicitarVerificacionCommand(SecurityUtils.actual().id(), archivos));
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new ConfianzaDtos.VerificacionResponse(solicitud.id(), solicitud.estado()));
    }

    @GetMapping("/verificaciones/mia")
    public ResponseEntity<ConfianzaDtos.VerificacionResponse> miVerificacion() {
        SecurityUtils.exigirRol("PRESTADOR");
        var solicitud = miVerificacionUseCase.ejecutar(new ConsultarVerificacionPropiaCommand(SecurityUtils.actual().id()));
        return ResponseEntity.ok(new ConfianzaDtos.VerificacionResponse(solicitud.id(), solicitud.estado()));
    }

    @PostMapping("/resenas/{id}/reportes")
    public ResponseEntity<ConfianzaDtos.ReporteResponse> reportarResena(@PathVariable UUID id,
            @Valid @RequestBody ConfianzaDtos.ReporteRequest req) {
        UUID reporteId = reporteUseCase.ejecutar(ConfianzaPresentationMapper.aCommand(id, SecurityUtils.actual().id(), req));
        return ResponseEntity.status(HttpStatus.CREATED).body(new ConfianzaDtos.ReporteResponse(reporteId));
    }
}
