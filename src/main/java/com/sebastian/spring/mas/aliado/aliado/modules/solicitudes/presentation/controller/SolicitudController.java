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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/api/v1/solicitudes")
@Tag(name = "Solicitudes", description = "Creación, consulta y gestión de solicitudes de servicio del cliente.")
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
    @Operation(summary = "Crear solicitud",
            description = "Crea una solicitud como borrador o publicada. Requiere el rol CLIENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Solicitud creada",
                    content = @Content(schema = @Schema(implementation = SolicitudDtos.SolicitudResponse.class))),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene el rol CLIENTE"),
            @ApiResponse(responseCode = "409", description = "La clave de idempotencia entra en conflicto")
    })
    public ResponseEntity<?> crear(@Valid @RequestBody SolicitudDtos.CrearRequest req,
            @Parameter(description = "Clave opcional para evitar crear solicitudes duplicadas al reintentar.")
            @RequestHeader(value = "Idempotency-Key", required = false) String claveHeader) {
        SecurityUtils.exigirRol("CLIENTE");
        UUID clienteId = SecurityUtils.actual().id();
        return ejecutarIdempotente(claveHeader, "solicitudes.crear", cuerpo -> clienteId, 201, () -> {
            return SolicitudPresentationMapper.aResponse(crearSolicitudUseCase.ejecutar(
                    SolicitudPresentationMapper.aCommand(clienteId, req)));
        });
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar solicitud",
            description = "Actualiza los datos de una solicitud propia que todavía admite cambios.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud actualizada",
                    content = @Content(schema = @Schema(implementation = SolicitudDtos.SolicitudResponse.class))),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación"),
            @ApiResponse(responseCode = "404", description = "La solicitud no existe o no pertenece al usuario")
    })
    public ResponseEntity<SolicitudDtos.SolicitudResponse> editar(@PathVariable UUID id,
            @Valid @RequestBody SolicitudDtos.EditarRequest req) {
        SecurityUtils.exigirRol("CLIENTE");
        return ResponseEntity.ok(SolicitudPresentationMapper.aResponse(editarSolicitudUseCase.ejecutar(
                SolicitudPresentationMapper.aCommand(id, SecurityUtils.actual().id(), req))));
    }

    @PostMapping("/{id}/publicar")
    @Operation(summary = "Publicar solicitud",
            description = "Publica un borrador propio que tenga la información requerida.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud publicada",
                    content = @Content(schema = @Schema(implementation = SolicitudDtos.SolicitudResponse.class))),
            @ApiResponse(responseCode = "400", description = "El borrador está incompleto o contiene datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación"),
            @ApiResponse(responseCode = "404", description = "La solicitud no existe o no pertenece al usuario"),
            @ApiResponse(responseCode = "409", description = "La solicitud no admite publicación")
    })
    public ResponseEntity<SolicitudDtos.SolicitudResponse> publicar(@PathVariable UUID id) {
        SecurityUtils.exigirRol("CLIENTE");
        return ResponseEntity.ok(SolicitudPresentationMapper.aResponse(publicarSolicitudUseCase.ejecutar(
                SolicitudPresentationMapper.aPublicarCommand(id, SecurityUtils.actual().id()))));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar solicitud", description = "Cancela una solicitud propia que admita cancelación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado",
                    content = @Content(schema = @Schema(implementation = SolicitudDtos.IdEstadoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación"),
            @ApiResponse(responseCode = "404", description = "La solicitud no existe o no pertenece al usuario"),
            @ApiResponse(responseCode = "409", description = "La solicitud no admite cancelación")
    })
    public ResponseEntity<SolicitudDtos.IdEstadoResponse> cancelar(@PathVariable UUID id) {
        SecurityUtils.exigirRol("CLIENTE");
        return ResponseEntity.ok(SolicitudPresentationMapper.aEstadoResponse(cancelarSolicitudUseCase.ejecutar(
                SolicitudPresentationMapper.aCancelarCommand(id, SecurityUtils.actual().id()))));
    }

    @GetMapping("/mias")
    @Operation(summary = "Listar mis solicitudes", description = "Lista las solicitudes asociadas al cliente autenticado.")
    @ApiResponse(responseCode = "200", description = "Solicitudes del cliente",
            content = @Content(array = @ArraySchema(
                    schema = @Schema(implementation = SolicitudDtos.SolicitudResponse.class))))
    public ResponseEntity<List<SolicitudDtos.SolicitudResponse>> mias() {
        SecurityUtils.exigirRol("CLIENTE");
        return ResponseEntity.ok(listarSolicitudesPropiasUseCase.ejecutar(SecurityUtils.actual().id()).stream()
                .map(SolicitudPresentationMapper::aResponse).toList());
    }

    @GetMapping("/mias/{id}")
    @Operation(summary = "Consultar solicitud propia", description = "Obtiene el detalle de una solicitud propia.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle de la solicitud",
                    content = @Content(schema = @Schema(implementation = SolicitudDtos.SolicitudResponse.class))),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación"),
            @ApiResponse(responseCode = "404", description = "La solicitud no existe o no pertenece al usuario")
    })
    public ResponseEntity<SolicitudDtos.SolicitudResponse> detalle(@PathVariable UUID id) {
        SecurityUtils.exigirRol("CLIENTE");
        return ResponseEntity.ok(SolicitudPresentationMapper.aResponse(consultarSolicitudPropiaUseCase.ejecutar(
                SolicitudPresentationMapper.aConsultaPropia(id, SecurityUtils.actual().id()))));
    }

    @PostMapping(value = "/{id}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Agregar foto a solicitud",
            description = "Adjunta una imagen JPEG, PNG o WebP de hasta 5 MB a una solicitud propia.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Foto agregada",
                    content = @Content(schema = @Schema(implementation = SolicitudDtos.FotoResponse.class))),
            @ApiResponse(responseCode = "400", description = "El archivo está vacío o no tiene nombre"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación"),
            @ApiResponse(responseCode = "404", description = "La solicitud no existe o no pertenece al usuario"),
            @ApiResponse(responseCode = "413", description = "El archivo supera el tamaño máximo"),
            @ApiResponse(responseCode = "415", description = "El formato del archivo no está permitido")
    })
    public ResponseEntity<SolicitudDtos.FotoResponse> agregarFoto(@PathVariable UUID id,
            @Parameter(description = "Imagen JPEG, PNG o WebP, con un tamaño máximo de 5 MB.")
            @RequestPart("archivo") MultipartFile archivo) {
        SecurityUtils.exigirRol("CLIENTE");
        var foto = agregarFotoSolicitudUseCase.ejecutar(SolicitudPresentationMapper.aFotoCommand(
                id, SecurityUtils.actual().id(), null, archivo.getOriginalFilename(), archivo.getContentType(), leerBytes(archivo)));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SolicitudPresentationMapper.aResponse(foto));
    }

    @GetMapping("/{id}/fotos/{fotoId}")
    @Operation(summary = "Descargar foto de solicitud",
            description = "Descarga una foto perteneciente a una solicitud accesible para el usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contenido binario de la foto",
                    content = @Content(mediaType = "application/octet-stream",
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación"),
            @ApiResponse(responseCode = "404", description = "La solicitud o la foto no está disponible")
    })
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
    @Operation(summary = "Reemplazar foto de solicitud",
            description = "Reemplaza una foto propia por una imagen JPEG, PNG o WebP de hasta 5 MB.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Foto reemplazada",
                    content = @Content(schema = @Schema(implementation = SolicitudDtos.FotoResponse.class))),
            @ApiResponse(responseCode = "400", description = "El archivo está vacío o no tiene nombre"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación"),
            @ApiResponse(responseCode = "404", description = "La solicitud o la foto no está disponible"),
            @ApiResponse(responseCode = "413", description = "El archivo supera el tamaño máximo"),
            @ApiResponse(responseCode = "415", description = "El formato del archivo no está permitido")
    })
    public ResponseEntity<SolicitudDtos.FotoResponse> reemplazarFoto(@PathVariable UUID id,
            @PathVariable UUID fotoId,
            @Parameter(description = "Imagen JPEG, PNG o WebP, con un tamaño máximo de 5 MB.")
            @RequestPart("archivo") MultipartFile archivo) {
        SecurityUtils.exigirRol("CLIENTE");
        var foto = reemplazarFotoSolicitudUseCase.ejecutar(SolicitudPresentationMapper.aFotoCommand(
                id, SecurityUtils.actual().id(), fotoId, archivo.getOriginalFilename(), archivo.getContentType(), leerBytes(archivo)));
        return ResponseEntity.ok(SolicitudPresentationMapper.aResponse(foto));
    }

    @DeleteMapping("/{id}/fotos/{fotoId}")
    @Operation(summary = "Eliminar foto de solicitud", description = "Elimina una foto de una solicitud propia.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Foto eliminada"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación"),
            @ApiResponse(responseCode = "404", description = "La solicitud o la foto no está disponible")
    })
    public ResponseEntity<Void> eliminarFoto(@PathVariable UUID id, @PathVariable UUID fotoId) {
        SecurityUtils.exigirRol("CLIENTE");
        eliminarFotoSolicitudUseCase.ejecutar(SolicitudPresentationMapper.aConsultarFotoCommand(
                id, SecurityUtils.actual().id(), fotoId));
        return ResponseEntity.noContent().build();
    }

    private static byte[] leerBytes(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException ex) {
            throw new BusinessException.BadRequest("ARCHIVO_INVALIDO", "No se pudo leer el archivo enviado.");
        }
    }
}
