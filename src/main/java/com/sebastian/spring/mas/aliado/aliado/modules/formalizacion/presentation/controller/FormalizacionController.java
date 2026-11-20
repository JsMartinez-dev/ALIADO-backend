package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.presentation.dto.FormalizacionDtos;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.presentation.mapper.FormalizacionPresentationMapper;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/formalizacion")
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
  public ResponseEntity<List<FormalizacionDtos.RutaResponse>> ruta() {
    SecurityUtils.exigirRol("PRESTADOR");
    return ResponseEntity.ok(
        rutas.ejecutar().stream().map(FormalizacionPresentationMapper::aResponse).toList());
  }

  @GetMapping("/enlaces/{id}")
  public ResponseEntity<FormalizacionDtos.EnlaceResponse> enlace(@PathVariable UUID id) {
    SecurityUtils.exigirRol("PRESTADOR");
    return ResponseEntity.ok(
        FormalizacionPresentationMapper.aResponse(
            enlaces.ejecutar(new ConsultarEnlaceInstitucionalCommand(id))));
  }

  @GetMapping("/progreso")
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
  public ResponseEntity<Void> vinculacion() {
    SecurityUtils.exigirRol("PRESTADOR");
    vinculacion.ejecutar();
    return ResponseEntity.noContent().build();
  }
}
