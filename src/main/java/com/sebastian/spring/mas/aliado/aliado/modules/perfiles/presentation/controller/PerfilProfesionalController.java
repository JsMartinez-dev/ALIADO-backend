package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ListarCompromisosAgendaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.EliminarServicioPortafolioUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.CrearCompromisoAgendaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ConsultarVistaPreviaPerfilUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ConsultarPerfilPropioUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ConfigurarDisponibilidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.AgregarServicioPortafolioUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ActualizarCompromisoAgendaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.RegistrarExperienciaProfesionalUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.EditarPerfilProfesionalUseCase;
import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.PerfilProfesionalService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.CrearPerfilProfesionalUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ActualizarCategoriasProfesionalesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ActualizarZonasAtencionUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.presentation.mapper.PerfilProfesionalPresentationMapper;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.EstadoExposicion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.presentation.dto.DatosGeneralesDtos;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.presentation.dto.PerfilProfesionalDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Perfil profesional del Prestador (SPEC-05). Todas las rutas exigen rol PRESTADOR. */
@RestController
@RequestMapping("/api/v1/perfiles")
@Tag(name = "Perfil profesional", description = "SPEC-05. Todas las operaciones requieren rol PRESTADOR.")
public class PerfilProfesionalController {
    private final PerfilProfesionalService service;
    private final ConsultarVistaPreviaPerfilUseCase vistaPreviaUseCase;
    private final ConsultarPerfilPropioUseCase perfilPropioUseCase;
    private final ListarCompromisosAgendaUseCase listarAgendaUseCase;
    private final ActualizarCompromisoAgendaUseCase actualizarCompromisoUseCase;
    private final CrearCompromisoAgendaUseCase crearCompromisoUseCase;
    private final ConfigurarDisponibilidadUseCase configurarDisponibilidadUseCase;
    private final CrearPerfilProfesionalUseCase crearPerfilProfesionalUseCase;
    private final ActualizarCategoriasProfesionalesUseCase actualizarCategoriasUseCase;
    private final ActualizarZonasAtencionUseCase actualizarZonasUseCase;
    private final EditarPerfilProfesionalUseCase editarPerfilUseCase;
    private final AgregarServicioPortafolioUseCase agregarPortafolioUseCase;
    private final EliminarServicioPortafolioUseCase eliminarPortafolioUseCase;
    private final RegistrarExperienciaProfesionalUseCase registrarExperienciaUseCase;

    public PerfilProfesionalController(PerfilProfesionalService service,
                                       CrearPerfilProfesionalUseCase crearPerfilProfesionalUseCase,
                                       ActualizarCategoriasProfesionalesUseCase actualizarCategoriasUseCase,
                                       ActualizarZonasAtencionUseCase actualizarZonasUseCase,
                                       RegistrarExperienciaProfesionalUseCase registrarExperienciaUseCase,
                                       EditarPerfilProfesionalUseCase editarPerfilUseCase,
                                       AgregarServicioPortafolioUseCase agregarPortafolioUseCase,
                                       EliminarServicioPortafolioUseCase eliminarPortafolioUseCase,
                                       ConfigurarDisponibilidadUseCase configurarDisponibilidadUseCase,
                                       CrearCompromisoAgendaUseCase crearCompromisoUseCase,
                                       ActualizarCompromisoAgendaUseCase actualizarCompromisoUseCase,
                                       ListarCompromisosAgendaUseCase listarAgendaUseCase,
                                       ConsultarVistaPreviaPerfilUseCase vistaPreviaUseCase,
                                       ConsultarPerfilPropioUseCase perfilPropioUseCase) {
        this.service = service;
        this.crearPerfilProfesionalUseCase = crearPerfilProfesionalUseCase;
        this.actualizarCategoriasUseCase = actualizarCategoriasUseCase;
        this.actualizarZonasUseCase = actualizarZonasUseCase;
        this.editarPerfilUseCase = editarPerfilUseCase;
        this.agregarPortafolioUseCase = agregarPortafolioUseCase;
        this.eliminarPortafolioUseCase = eliminarPortafolioUseCase;
        this.vistaPreviaUseCase = vistaPreviaUseCase;
        this.perfilPropioUseCase = perfilPropioUseCase;
        this.listarAgendaUseCase = listarAgendaUseCase;
        this.actualizarCompromisoUseCase = actualizarCompromisoUseCase;
        this.crearCompromisoUseCase = crearCompromisoUseCase;
        this.configurarDisponibilidadUseCase = configurarDisponibilidadUseCase;
        this.registrarExperienciaUseCase = registrarExperienciaUseCase;
    }

    private PerfilProfesionalDtos.PerfilResponse aRespuesta(PerfilProfesional p) {
        return new PerfilProfesionalDtos.PerfilResponse(p.id(), p.prestadorId(),
                DatosGeneralesDtos.DatosGeneralesResponse.desde(p.datosGenerales()),
                p.estadoExposicion().name(), List.copyOf(p.categoriaIds()), List.copyOf(p.zonaIds()),
                p.experiencias().stream().map(e -> new PerfilProfesionalDtos.ExperienciaResponse(
                        e.id(), e.descripcion(), e.fechaInicio(), e.fechaFin())).toList(),
                p.portafolio().stream().map(s -> new PerfilProfesionalDtos.PortafolioResponse(
                        s.id(), s.descripcion(), s.evidenciaKey(), s.fechaCreacion())).toList(),
                p.disponibilidades().stream().map(d -> new PerfilProfesionalDtos.DisponibilidadResponse(
                        d.id(), d.diaSemana(), d.horaInicio(), d.horaFin())).toList(),
                p.compromisos().stream().map(c -> new PerfilProfesionalDtos.CompromisoResponse(
                        c.id(), c.fechaHoraInicio(), c.fechaHoraFin(), c.origen().name())).toList());
    }

    @PostMapping
    public ResponseEntity<PerfilProfesionalDtos.PerfilResponse> crear(
            @Valid @RequestBody PerfilProfesionalDtos.CrearRequest req) {
        SecurityUtils.exigirRol("PRESTADOR");
        var resultado = crearPerfilProfesionalUseCase.ejecutar(
                PerfilProfesionalPresentationMapper.aCommand(SecurityUtils.actual().id(), req));
        return ResponseEntity.status(HttpStatus.CREATED).body(PerfilProfesionalPresentationMapper.aResponse(resultado));
    }

    @PutMapping("/mio")
    public ResponseEntity<PerfilProfesionalDtos.PerfilResponse> actualizar(
            @Valid @RequestBody PerfilProfesionalDtos.ActualizarDatosRequest req) {
        SecurityUtils.exigirRol("PRESTADOR");
        return ResponseEntity.ok(PerfilProfesionalPresentationMapper.aResponse(editarPerfilUseCase.ejecutar(
                PerfilProfesionalPresentationMapper.aEditarCommand(SecurityUtils.actual().id(), req))));
    }

    @PutMapping("/mio/categorias")
    public ResponseEntity<PerfilProfesionalDtos.PerfilResponse> categorias(
            @Valid @RequestBody PerfilProfesionalDtos.IdsRequest req) {
        SecurityUtils.exigirRol("PRESTADOR");
        return ResponseEntity.ok(PerfilProfesionalPresentationMapper.aResponse(actualizarCategoriasUseCase.ejecutar(
                PerfilProfesionalPresentationMapper.aCategoriasCommand(SecurityUtils.actual().id(), req))));
    }

    @PutMapping("/mio/zonas")
    public ResponseEntity<PerfilProfesionalDtos.PerfilResponse> zonas(
            @Valid @RequestBody PerfilProfesionalDtos.IdsRequest req) {
        SecurityUtils.exigirRol("PRESTADOR");
        return ResponseEntity.ok(PerfilProfesionalPresentationMapper.aResponse(actualizarZonasUseCase.ejecutar(
                PerfilProfesionalPresentationMapper.aZonasCommand(SecurityUtils.actual().id(), req))));
    }

    @PostMapping("/mio/experiencia")
    public ResponseEntity<PerfilProfesionalDtos.ExperienciaResponse> experiencia(
            @Valid @RequestBody PerfilProfesionalDtos.ExperienciaRequest req) {
        SecurityUtils.exigirRol("PRESTADOR");
        var resultado = registrarExperienciaUseCase.ejecutar(
                PerfilProfesionalPresentationMapper.aExperienciaCommand(SecurityUtils.actual().id(), req));
        return ResponseEntity.status(HttpStatus.CREATED).body(PerfilProfesionalPresentationMapper.aExperienciaResponse(resultado));
    }

    @PostMapping(value = "/mio/portafolio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PerfilProfesionalDtos.PortafolioResponse> portafolio(
            @RequestPart("descripcion") String descripcion,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo) {
        SecurityUtils.exigirRol("PRESTADOR");
        byte[] contenido = null;
        if (archivo != null) {
            try {
                contenido = archivo.getBytes();
            } catch (IOException ex) {
                throw new BusinessException.BadRequest("ARCHIVO_INVALIDO",
                        "No se pudo leer el archivo enviado.");
            }
        }
        var resultado = agregarPortafolioUseCase.ejecutar(PerfilProfesionalPresentationMapper.aPortafolioCommand(
                SecurityUtils.actual().id(), descripcion,
                archivo == null ? null : archivo.getOriginalFilename(),
                archivo == null ? null : archivo.getContentType(), contenido));
        return ResponseEntity.status(HttpStatus.CREATED).body(PerfilProfesionalPresentationMapper.aPortafolioResponse(resultado));
    }

    @DeleteMapping("/mio/portafolio/{id}")
    public ResponseEntity<Void> eliminarPortafolio(@PathVariable UUID id) {
        SecurityUtils.exigirRol("PRESTADOR");
        eliminarPortafolioUseCase.ejecutar(PerfilProfesionalPresentationMapper.aEliminarPortafolioCommand(SecurityUtils.actual().id(), id));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/mio/disponibilidad")
    public ResponseEntity<PerfilProfesionalDtos.PerfilResponse> disponibilidad(
            @Valid @RequestBody PerfilProfesionalDtos.DisponibilidadRequest req) {
        SecurityUtils.exigirRol("PRESTADOR");
        return ResponseEntity.ok(PerfilProfesionalPresentationMapper.aResponse(configurarDisponibilidadUseCase.ejecutar(
                PerfilProfesionalPresentationMapper.aDisponibilidadCommand(SecurityUtils.actual().id(), req))));
    }

    @PostMapping("/mio/agenda")
    public ResponseEntity<PerfilProfesionalDtos.CompromisoResponse> crearCompromiso(
            @Valid @RequestBody PerfilProfesionalDtos.CompromisoRequest req) {
        SecurityUtils.exigirRol("PRESTADOR");
        var resultado = crearCompromisoUseCase.ejecutar(PerfilProfesionalPresentationMapper.aCompromisoCommand(SecurityUtils.actual().id(), req));
        return ResponseEntity.status(HttpStatus.CREATED).body(PerfilProfesionalPresentationMapper.aCompromisoResponse(resultado));
    }

    @PutMapping("/mio/agenda")
    public ResponseEntity<PerfilProfesionalDtos.CompromisoResponse> actualizarCompromiso(
            @Valid @RequestBody PerfilProfesionalDtos.CompromisoRequest req) {
        SecurityUtils.exigirRol("PRESTADOR");
        var resultado = actualizarCompromisoUseCase.ejecutar(PerfilProfesionalPresentationMapper.aCompromisoCommand(SecurityUtils.actual().id(), req));
        return ResponseEntity.ok(PerfilProfesionalPresentationMapper.aCompromisoResponse(resultado));
    }

    @GetMapping("/mio")
    @Operation(summary = "Consultar perfil profesional propio",
            description = "Requiere rol PRESTADOR. Devuelve el perfil completo del usuario autenticado.")
    @ApiResponse(responseCode = "404", description = "El Prestador aún no ha creado su perfil")
    public ResponseEntity<PerfilProfesionalDtos.PerfilResponse> propio() {
        SecurityUtils.exigirRol("PRESTADOR");
        return ResponseEntity.ok(PerfilProfesionalPresentationMapper.aResponse(
                perfilPropioUseCase.ejecutar(SecurityUtils.actual().id())));
    }

    @GetMapping("/mio/agenda")
    public ResponseEntity<List<PerfilProfesionalDtos.CompromisoResponse>> agenda() {
        SecurityUtils.exigirRol("PRESTADOR");
        return ResponseEntity.ok(listarAgendaUseCase.ejecutar(SecurityUtils.actual().id()).stream()
                .map(PerfilProfesionalPresentationMapper::aCompromisoResponse).toList());
    }

    @GetMapping("/mio/vista-previa")
    public ResponseEntity<PerfilProfesionalDtos.VistaPreviaResponse> vistaPrevia() {
        SecurityUtils.exigirRol("PRESTADOR");
        return ResponseEntity.ok(PerfilProfesionalPresentationMapper.aVistaPreviaResponse(
                vistaPreviaUseCase.ejecutar(SecurityUtils.actual().id())));
    }

    @PutMapping("/mio/exposicion")
    public ResponseEntity<PerfilProfesionalDtos.PerfilResponse> exposicion(
            @Valid @RequestBody PerfilProfesionalDtos.ExposicionRequest req) {
        SecurityUtils.exigirRol("PRESTADOR");
        EstadoExposicion nuevo;
        try {
            nuevo = EstadoExposicion.valueOf(req.estadoExposicion().trim());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new BusinessException.BadRequest("EXPOSICION_INVALIDA",
                    "estadoExposicion debe ser PUBLICO u OCULTO.");
        }
        return ResponseEntity.ok(aRespuesta(
                service.cambiarExposicion(SecurityUtils.actual().id(), nuevo)));
    }
}
