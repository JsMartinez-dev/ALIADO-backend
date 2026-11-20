package com.sebastian.spring.mas.aliado.aliado.modules.administracion.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.presentation.dto.AdminDtos;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.presentation.mapper.AdminPresentationMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Búsqueda administrativa (SPEC-12, UC075). Solo rol ADMINISTRADOR (ver SecurityConfig). */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminBusquedaController {
  private final BuscarCuentasPerfilesUseCase buscarCuentasPerfilesUseCase;

  public AdminBusquedaController(BuscarCuentasPerfilesUseCase buscarCuentasPerfilesUseCase) {
    this.buscarCuentasPerfilesUseCase = buscarCuentasPerfilesUseCase;
  }

  @GetMapping("/busqueda")
  public ResponseEntity<AdminDtos.BusquedaResponse> busqueda(@RequestParam String q) {
    var resultado =
        buscarCuentasPerfilesUseCase.ejecutar(new BuscarCuentasPerfilesCommand(q.trim()));
    return ResponseEntity.ok(AdminPresentationMapper.aResponse(resultado));
  }
}
