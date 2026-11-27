package com.sebastian.spring.mas.aliado.aliado.modules.administracion.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.presentation.dto.AdminDtos;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.presentation.mapper.AdminPresentationMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminCatalogoController {
  private final CrearCategoriaUseCase crearCategoriaUseCase;
  private final ListarCategoriasUseCase listarCategoriasUseCase;
  private final EditarCategoriaUseCase editarCategoriaUseCase;
  private final DesactivarCategoriaUseCase desactivarCategoriaUseCase;
  private final CrearZonaUseCase crearZonaUseCase;
  private final ListarZonasUseCase listarZonasUseCase;
  private final EditarZonaUseCase editarZonaUseCase;
  private final DesactivarZonaUseCase desactivarZonaUseCase;

  public AdminCatalogoController(
      CrearCategoriaUseCase crearCategoriaUseCase,
      ListarCategoriasUseCase listarCategoriasUseCase,
      EditarCategoriaUseCase editarCategoriaUseCase,
      DesactivarCategoriaUseCase desactivarCategoriaUseCase,
      CrearZonaUseCase crearZonaUseCase,
      ListarZonasUseCase listarZonasUseCase,
      EditarZonaUseCase editarZonaUseCase,
      DesactivarZonaUseCase desactivarZonaUseCase) {
    this.crearCategoriaUseCase = crearCategoriaUseCase;
    this.listarCategoriasUseCase = listarCategoriasUseCase;
    this.editarCategoriaUseCase = editarCategoriaUseCase;
    this.desactivarCategoriaUseCase = desactivarCategoriaUseCase;
    this.crearZonaUseCase = crearZonaUseCase;
    this.listarZonasUseCase = listarZonasUseCase;
    this.editarZonaUseCase = editarZonaUseCase;
    this.desactivarZonaUseCase = desactivarZonaUseCase;
  }

  @PostMapping("/categorias")
  public ResponseEntity<AdminDtos.CategoriaResponse> crearCategoria(
      @Valid @RequestBody AdminDtos.CategoriaRequest req) {
    var c =
        crearCategoriaUseCase.ejecutar(
            new CrearCategoriaCommand(SecurityUtils.actual().id(), req.nombre()));
    return ResponseEntity.status(HttpStatus.CREATED).body(AdminPresentationMapper.aResponse(c));
  }

  @GetMapping("/categorias")
  public ResponseEntity<List<AdminDtos.CategoriaResponse>> listarCategorias() {
    return ResponseEntity.ok(
        listarCategoriasUseCase.ejecutar().stream()
            .map(c -> AdminPresentationMapper.aResponse(c))
            .toList());
  }

  @PutMapping("/categorias/{id}")
  public ResponseEntity<AdminDtos.CategoriaResponse> actualizarCategoria(
      @PathVariable UUID id, @Valid @RequestBody AdminDtos.CategoriaUpdateRequest req) {
    var c =
        editarCategoriaUseCase.ejecutar(
            new EditarCategoriaCommand(
                SecurityUtils.actual().id(), id, req.nombre(), req.estado()));
    return ResponseEntity.ok(AdminPresentationMapper.aResponse(c));
  }

  @DeleteMapping("/categorias/{id}")
  public ResponseEntity<Void> desactivarCategoria(@PathVariable UUID id) {
    desactivarCategoriaUseCase.ejecutar(
        new DesactivarCategoriaCommand(SecurityUtils.actual().id(), id));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/zonas")
  public ResponseEntity<AdminDtos.ZonaResponse> crearZona(
      @Valid @RequestBody AdminDtos.ZonaRequest req) {
    var z =
        crearZonaUseCase.ejecutar(
            new CrearZonaCommand(
                SecurityUtils.actual().id(), req.nombre(), req.coberturaGeografica()));
    return ResponseEntity.status(HttpStatus.CREATED).body(AdminPresentationMapper.aResponse(z));
  }

  @GetMapping("/zonas")
  public ResponseEntity<List<AdminDtos.ZonaResponse>> listarZonas() {
    return ResponseEntity.ok(
        listarZonasUseCase.ejecutar().stream()
            .map(z -> AdminPresentationMapper.aResponse(z))
            .toList());
  }

  @PutMapping("/zonas/{id}")
  public ResponseEntity<AdminDtos.ZonaResponse> actualizarZona(
      @PathVariable UUID id, @Valid @RequestBody AdminDtos.ZonaUpdateRequest req) {
    var z =
        editarZonaUseCase.ejecutar(
            new EditarZonaCommand(
                SecurityUtils.actual().id(),
                id,
                req.nombre(),
                req.coberturaGeografica(),
                req.estado()));
    return ResponseEntity.ok(AdminPresentationMapper.aResponse(z));
  }

  @DeleteMapping("/zonas/{id}")
  public ResponseEntity<Void> desactivarZona(@PathVariable UUID id) {
    desactivarZonaUseCase.ejecutar(new DesactivarZonaCommand(SecurityUtils.actual().id(), id));
    return ResponseEntity.noContent().build();
  }
}
