package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoValidacion;
import com.sebastian.spring.mas.aliado.aliado.common.web.BaseController;
import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.common.idempotencia.IdempotenciaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.ConsultarSolicitudPropiaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.ListarSolicitudesPropiasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.CancelarSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.EditarSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.AgregarFotoSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.ReemplazarFotoSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.EliminarFotoSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.DescargarFotoSolicitudUseCase;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.CrearSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.PublicarSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.presentation.mapper.SolicitudPresentationMapper;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.presentation.dto.SolicitudDtos;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/* Todas las rutas exigen rol CLIENTE. */
@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController extends BaseController {
    private final ConsultarSolicitudPropiaUseCase consultarSolicitudPropiaUseCase;
    private final ListarSolicitudesPropiasUseCase listarSolicitudesPropiasUseCase;
    private final CancelarSolicitudUseCase cancelarSolicitudUseCase;
    private final EditarSolicitudUseCase editarSolicitudUseCase;
    private final AgregarFotoSolicitudUseCase agregarFotoSolicitudUseCase;
    private final ReemplazarFotoSolicitudUseCase reemplazarFotoSolicitudUseCase;
    private final EliminarFotoSolicitudUseCase eliminarFotoSolicitudUseCase;
    private final DescargarFotoSolicitudUseCase descargarFotoSolicitudUseCase;

    private final CrearSolicitudUseCase crearSolicitudUseCase;
    private final PublicarSolicitudUseCase publicarSolicitudUseCase;

    public SolicitudController(ConsultarSolicitudPropiaUseCase consultarSolicitudPropiaUseCase, CrearSolicitudUseCase crearSolicitudUseCase,
                               PublicarSolicitudUseCase publicarSolicitudUseCase,
                               AgregarFotoSolicitudUseCase agregarFotoSolicitudUseCase,
                               ReemplazarFotoSolicitudUseCase reemplazarFotoSolicitudUseCase,
                               EliminarFotoSolicitudUseCase eliminarFotoSolicitudUseCase,
                               DescargarFotoSolicitudUseCase descargarFotoSolicitudUseCase,
                               EditarSolicitudUseCase editarSolicitudUseCase,
                               CancelarSolicitudUseCase cancelarSolicitudUseCase,
                               ListarSolicitudesPropiasUseCase listarSolicitudesPropiasUseCase,
                               ObjectMapper objectMapper, IdempotenciaPort idempotencyService) {
        super(objectMapper, idempotencyService);
        this.consultarSolicitudPropiaUseCase = consultarSolicitudPropiaUseCase;
        this.listarSolicitudesPropiasUseCase = listarSolicitudesPropiasUseCase;
        this.cancelarSolicitudUseCase = cancelarSolicitudUseCase;
        this.editarSolicitudUseCase = editarSolicitudUseCase;
        this.agregarFotoSolicitudUseCase = agregarFotoSolicitudUseCase;
        this.reemplazarFotoSolicitudUseCase = reemplazarFotoSolicitudUseCase;
        this.eliminarFotoSolicitudUseCase = eliminarFotoSolicitudUseCase;
        this.descargarFotoSolicitudUseCase = descargarFotoSolicitudUseCase;

        this.crearSolicitudUseCase = crearSolicitudUseCase;
        this.publicarSolicitudUseCase = publicarSolicitudUseCase;
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody SolicitudDtos.CrearRequest req,
                                   @RequestHeader(value = "Idempotency-Key", required = false) String claveHeader) {
        SecurityUtils.exigirRol("CLIENTE");
        UUID clienteId = SecurityUtils.actual().id();
        return ejecutarIdempotente(claveHeader, "solicitudes.crear", cuerpo -> clienteId, 201, () -> {
            return SolicitudPresentationMapper.aResponse(crearSolicitudUseCase.ejecutar(
                    SolicitudPresentationMapper.aCommand(clienteId, req)));
        });
    }

    @PutMapping("/{id}")
    public ResponseEntity<SolicitudDtos.SolicitudResponse> editar(@PathVariable UUID id,
            @Valid @RequestBody SolicitudDtos.EditarRequest req) {
        SecurityUtils.exigirRol("CLIENTE");
        return ResponseEntity.ok(SolicitudPresentationMapper.aResponse(editarSolicitudUseCase.ejecutar(
                SolicitudPresentationMapper.aCommand(id, SecurityUtils.actual().id(), req))));
    }

    @PostMapping("/{id}/publicar")
    public ResponseEntity<SolicitudDtos.SolicitudResponse> publicar(@PathVariable UUID id) {
        SecurityUtils.exigirRol("CLIENTE");
        return ResponseEntity.ok(SolicitudPresentationMapper.aResponse(publicarSolicitudUseCase.ejecutar(
                SolicitudPresentationMapper.aPublicarCommand(id, SecurityUtils.actual().id()))));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<SolicitudDtos.IdEstadoResponse> cancelar(@PathVariable UUID id) {
        SecurityUtils.exigirRol("CLIENTE");
        return ResponseEntity.ok(SolicitudPresentationMapper.aEstadoResponse(cancelarSolicitudUseCase.ejecutar(
                SolicitudPresentationMapper.aCancelarCommand(id, SecurityUtils.actual().id()))));
    }

    @GetMapping("/mias")
    public ResponseEntity<List<SolicitudDtos.SolicitudResponse>> mias() {
        SecurityUtils.exigirRol("CLIENTE");
        return ResponseEntity.ok(listarSolicitudesPropiasUseCase.ejecutar(SecurityUtils.actual().id()).stream()
                .map(SolicitudPresentationMapper::aResponse).toList());
    }

    @GetMapping("/mias/{id}")
    public ResponseEntity<SolicitudDtos.SolicitudResponse> detalle(@PathVariable UUID id) {
        SecurityUtils.exigirRol("CLIENTE");
        return ResponseEntity.ok(SolicitudPresentationMapper.aResponse(consultarSolicitudPropiaUseCase.ejecutar(
                SolicitudPresentationMapper.aConsultaPropia(id, SecurityUtils.actual().id()))));
    }

    @PostMapping(value = "/{id}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SolicitudDtos.FotoResponse> agregarFoto(@PathVariable UUID id,
            @RequestPart("archivo") MultipartFile archivo) {
        SecurityUtils.exigirRol("CLIENTE");
        var foto = agregarFotoSolicitudUseCase.ejecutar(SolicitudPresentationMapper.aFotoCommand(
                id, SecurityUtils.actual().id(), null, archivo.getOriginalFilename(), archivo.getContentType(), leerBytes(archivo)));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SolicitudPresentationMapper.aResponse(foto));
    }

    @GetMapping("/{id}/fotos/{fotoId}")
    public ResponseEntity<byte[]> descargarFoto(@PathVariable UUID id, @PathVariable UUID fotoId) {
        byte[] contenido = descargarFotoSolicitudUseCase.ejecutar(SolicitudPresentationMapper.aConsultarFotoCommand(
                id, SecurityUtils.actual().id(), fotoId)).contenido();
        String tipo = ArchivoValidacion.detectarTipo(contenido);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        tipo == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : tipo))
                .body(contenido);
    }

    @PutMapping(value = "/{id}/fotos/{fotoId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SolicitudDtos.FotoResponse> reemplazarFoto(@PathVariable UUID id,
            @PathVariable UUID fotoId, @RequestPart("archivo") MultipartFile archivo) {
        SecurityUtils.exigirRol("CLIENTE");
        var foto = reemplazarFotoSolicitudUseCase.ejecutar(SolicitudPresentationMapper.aFotoCommand(
                id, SecurityUtils.actual().id(), fotoId, archivo.getOriginalFilename(), archivo.getContentType(), leerBytes(archivo)));
        return ResponseEntity.ok(SolicitudPresentationMapper.aResponse(foto));
    }

    @DeleteMapping("/{id}/fotos/{fotoId}")
    public ResponseEntity<Void> eliminarFoto(@PathVariable UUID id, @PathVariable UUID fotoId) {
        SecurityUtils.exigirRol("CLIENTE");
        eliminarFotoSolicitudUseCase.ejecutar(SolicitudPresentationMapper.aConsultarFotoCommand(
                id, SecurityUtils.actual().id(), fotoId));
        return ResponseEntity.noContent().build();
    }

    /** Un fallo de lectura del multipart es un 400, no un 500 (SPEC-10/11). */
    private static byte[] leerBytes(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException ex) {
            throw new BusinessException.BadRequest("ARCHIVO_INVALIDO", "No se pudo leer el archivo enviado.");
        }
    }
}
