package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.presentation.dto.FormalizacionDtos;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.presentation.mapper.FormalizacionPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/formalizacion")
@Tag(name = "Formalización", description = "SPEC-13. Todas las operaciones requieren rol PRESTADOR.")
public class FormalizacionController {
  private final ConsultarRutaFormalizacionUseCase rutas;
  private final ConsultarEnlaceInstitucionalUseCase enlaces;
  private final ConsultarProgresoFormalizacionUseCase progreso;
  private final ActualizarProgresoFormalizacionUseCase actualizar;
  private final ConsultarVinculacionInstitucionalUseCase vinculacion;

  public FormalizacionController(
      ConsultarRutaFormalizacionUseCase rutas,
      ConsultarEnlaceInstitucionalUseCase enlaces,
      ConsultarProgresoFormalizacionUseCase progreso,
      ActualizarProgresoFormalizacionUseCase actualizar,
      ConsultarVinculacionInstitucionalUseCase vinculacion) {
    this.rutas = rutas;
    this.enlaces = enlaces;
    this.progreso = progreso;
    this.actualizar = actualizar;
    this.vinculacion = vinculacion;
  }

  @GetMapping("/ruta")
  @Operation(summary = "Consultar ruta de formalización",
      description = "Requiere rol PRESTADOR. Devuelve un arreglo de rutas con pasos y enlaces.")
  public ResponseEntity<List<FormalizacionDtos.RutaResponse>> ruta() {
    SecurityUtils.exigirRol("PRESTADOR");
    return ResponseEntity.ok(
        rutas.ejecutar().stream().map(FormalizacionPresentationMapper::aResponse).toList());
  }

  @GetMapping("/enlaces/{id}")
  @Operation(summary = "Consultar enlace institucional",
      description = "Requiere rol PRESTADOR. El estado puede ser DISPONIBLE o NO_DISPONIBLE.")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "Enlace encontrado"),
      @ApiResponse(responseCode = "404", description = "Enlace no encontrado")})
  public ResponseEntity<FormalizacionDtos.EnlaceResponse> enlace(@PathVariable UUID id) {
    SecurityUtils.exigirRol("PRESTADOR");
    return ResponseEntity.ok(
        FormalizacionPresentationMapper.aResponse(
            enlaces.ejecutar(new ConsultarEnlaceInstitucionalCommand(id))));
  }

  @GetMapping("/progreso")
  @Operation(summary = "Consultar progreso de formalización",
      description = "Requiere rol PRESTADOR. Devuelve un arreglo con avisoLegal.")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "Progreso del prestador"),
      @ApiResponse(responseCode = "404", description = "SIN_PROGRESO: el prestador no tiene progreso registrado")})
  public ResponseEntity<List<FormalizacionDtos.ProgresoResponse>> progreso() {
    SecurityUtils.exigirRol("PRESTADOR");
    return ResponseEntity.ok(
        progreso
            .ejecutar(new ConsultarProgresoFormalizacionCommand(SecurityUtils.actual().id()))
            .stream()
            .map(FormalizacionPresentationMapper::aResponse)
            .toList());
  }

  @PutMapping("/progreso")
  @Operation(summary = "Actualizar progreso orientativo",
      description = "Requiere rol PRESTADOR. rutaId y valor son obligatorios; valor admite 0..100.")
  public ResponseEntity<FormalizacionDtos.ProgresoResponse> actualizarProgreso(
      @Valid @RequestBody FormalizacionDtos.ActualizarProgresoRequest req) {
    SecurityUtils.exigirRol("PRESTADOR");
    return ResponseEntity.ok(
        FormalizacionPresentationMapper.aResponse(
            actualizar.ejecutar(
                new ActualizarProgresoFormalizacionCommand(
                    SecurityUtils.actual().id(), req.rutaId(), req.valor()))));
  }

  @GetMapping("/vinculacion")
  @Operation(summary = "Consultar vinculación institucional",
      description = "Requiere rol PRESTADOR. Responde 204 sin contenido.")
  public ResponseEntity<Void> vinculacion() {
    SecurityUtils.exigirRol("PRESTADOR");
    vinculacion.ejecutar();
    return ResponseEntity.noContent().build();
  }
}
