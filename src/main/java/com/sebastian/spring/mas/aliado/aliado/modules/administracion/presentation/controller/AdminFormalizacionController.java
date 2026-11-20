package com.sebastian.spring.mas.aliado.aliado.modules.administracion.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.presentation.dto.AdminDtos;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.usecase.ActualizarEnlaceInstitucionalUseCase;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Administración de formalización (SPEC-13). Solo rol ADMINISTRADOR (ver SecurityConfig). */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminFormalizacionController {
  private final ActualizarEnlaceInstitucionalUseCase formalizacion;

  public AdminFormalizacionController(ActualizarEnlaceInstitucionalUseCase formalizacion) {
    this.formalizacion = formalizacion;
  }

  @PutMapping("/formalizacion/enlaces/{id}")
  public ResponseEntity<AdminDtos.EnlaceResponse> actualizarEnlace(
      @PathVariable UUID id, @RequestBody AdminDtos.ActualizarEnlaceRequest req) {
    var e = formalizacion.ejecutar(new com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto.ActualizarEnlaceInstitucionalCommand(id, req.nombre(), req.url(), req.estado()));
    return ResponseEntity.ok(
        new AdminDtos.EnlaceResponse(e.id(), e.nombre(), e.url(), e.estado()));
  }
}
