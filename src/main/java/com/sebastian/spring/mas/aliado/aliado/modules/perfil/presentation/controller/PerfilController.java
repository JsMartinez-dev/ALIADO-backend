package com.sebastian.spring.mas.aliado.aliado.modules.perfil.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConsultarPerfilUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ActualizarPerfilUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConfigurarUbicacionUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConfigurarPrivacidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConsultarPerfilPublicoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.presentation.mapper.PerfilPresentationMapper;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.SolicitarEliminacionCuentaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConfirmarEliminacionCuentaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.presentation.dto.PerfilDtos;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/perfil")
public class PerfilController {
    private final SolicitarEliminacionCuentaUseCase solicitarEliminacionCuentaUseCase;
    private final ConfirmarEliminacionCuentaUseCase confirmarEliminacionCuentaUseCase;
    private final ConsultarPerfilUseCase consultarPerfilUseCase;
    private final ActualizarPerfilUseCase actualizarPerfilUseCase;
    private final ConfigurarUbicacionUseCase configurarUbicacionUseCase;
    private final ConfigurarPrivacidadUseCase configurarPrivacidadUseCase;
    private final ConsultarPerfilPublicoUseCase consultarPerfilPublicoUseCase;

    public PerfilController(SolicitarEliminacionCuentaUseCase solicitarEliminacionCuentaUseCase,
                            ConfirmarEliminacionCuentaUseCase confirmarEliminacionCuentaUseCase,
                            ConsultarPerfilUseCase consultarPerfilUseCase,
                            ActualizarPerfilUseCase actualizarPerfilUseCase,
                            ConfigurarUbicacionUseCase configurarUbicacionUseCase,
                            ConfigurarPrivacidadUseCase configurarPrivacidadUseCase,
                            ConsultarPerfilPublicoUseCase consultarPerfilPublicoUseCase) {
        this.solicitarEliminacionCuentaUseCase = solicitarEliminacionCuentaUseCase;
        this.confirmarEliminacionCuentaUseCase = confirmarEliminacionCuentaUseCase;
        this.consultarPerfilUseCase = consultarPerfilUseCase;
        this.actualizarPerfilUseCase = actualizarPerfilUseCase;
        this.configurarUbicacionUseCase = configurarUbicacionUseCase;
        this.configurarPrivacidadUseCase = configurarPrivacidadUseCase;
        this.consultarPerfilPublicoUseCase = consultarPerfilPublicoUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<PerfilDtos.PerfilResponse> me() {
        return ResponseEntity.ok(PerfilPresentationMapper.aResponse(
                consultarPerfilUseCase.ejecutar(SecurityUtils.actual().id())));
    }

    @PatchMapping("/me")
    public ResponseEntity<PerfilDtos.PerfilResponse> actualizar(
            @Valid @RequestBody PerfilDtos.ActualizarPerfilRequest req) {
        return ResponseEntity.ok(PerfilPresentationMapper.aResponse(actualizarPerfilUseCase.ejecutar(
                PerfilPresentationMapper.aCommand(SecurityUtils.actual().id(), req))));
    }

    @PutMapping("/me/ubicacion")
    public ResponseEntity<PerfilDtos.PerfilResponse> ubicacion(
            @Valid @RequestBody PerfilDtos.UbicacionRequest req) {
        return ResponseEntity.ok(PerfilPresentationMapper.aResponse(configurarUbicacionUseCase.ejecutar(
                PerfilPresentationMapper.aCommand(SecurityUtils.actual().id(), req))));
    }

    @PatchMapping("/me/visibilidad")
    public ResponseEntity<PerfilDtos.PerfilResponse> visibilidad(
            @Valid @RequestBody PerfilDtos.VisibilidadRequest req) {
        return ResponseEntity.ok(PerfilPresentationMapper.aResponse(configurarPrivacidadUseCase.ejecutar(
                PerfilPresentationMapper.aCommand(SecurityUtils.actual().id(), req))));
    }

    @GetMapping("/{usuarioId}/publico")
    public ResponseEntity<PerfilDtos.PerfilPublicoResponse> publico(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(PerfilPresentationMapper.aResponse(consultarPerfilPublicoUseCase.ejecutar(usuarioId)));
    }

    @PostMapping("/me/eliminacion")
    public ResponseEntity<PerfilDtos.EliminacionResponse> solicitarEliminacion() {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(PerfilPresentationMapper.aResponse(solicitarEliminacionCuentaUseCase.ejecutar(SecurityUtils.actual().id())));
    }

    @PostMapping("/me/eliminacion/confirmar")
    public ResponseEntity<PerfilDtos.EliminacionResponse> confirmarEliminacion() {
        return ResponseEntity.ok(PerfilPresentationMapper.aResponse(confirmarEliminacionCuentaUseCase.ejecutar(SecurityUtils.actual().id())));
    }
}
