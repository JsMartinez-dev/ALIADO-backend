package com.sebastian.spring.mas.aliado.aliado.modules.administracion.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.presentation.dto.AdminDtos;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.presentation.mapper.AdminPresentationMapper;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Moderación y bloqueos de cuentas (SPEC-12). Solo rol ADMINISTRADOR (ver SecurityConfig). */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminModeracionController {
  private final BloquearCuentaUseCase bloquearCuentaUseCase;
  private final LevantarBloqueoUseCase levantarBloqueoUseCase;
  private final ModerarContenidoUseCase moderarContenidoUseCase;
  private final ResolverVerificacionAdministrativaUseCase resolverVerificacionAdministrativaUseCase;

  public AdminModeracionController(
      BloquearCuentaUseCase bloquearCuentaUseCase,
      LevantarBloqueoUseCase levantarBloqueoUseCase,
      ModerarContenidoUseCase moderarContenidoUseCase,
      ResolverVerificacionAdministrativaUseCase resolverVerificacionAdministrativaUseCase) {
    this.bloquearCuentaUseCase = bloquearCuentaUseCase;
    this.levantarBloqueoUseCase = levantarBloqueoUseCase;
    this.moderarContenidoUseCase = moderarContenidoUseCase;
    this.resolverVerificacionAdministrativaUseCase = resolverVerificacionAdministrativaUseCase;
  }

  @PostMapping("/usuarios/{id}/bloqueos")
  public ResponseEntity<AdminDtos.BloqueoResponse> bloquearUsuario(
      @PathVariable UUID id, @Valid @RequestBody AdminDtos.BloqueoRequest req) {
    var b =
        bloquearCuentaUseCase.ejecutar(
            new BloquearCuentaCommand(id, SecurityUtils.actual().id(), req.motivo()));
    return ResponseEntity.status(HttpStatus.CREATED).body(AdminPresentationMapper.aResponse(b));
  }

  @PostMapping("/usuarios/{id}/bloqueos/{bloqueoId}/levantar")
  public ResponseEntity<Void> levantarBloqueo(@PathVariable UUID id, @PathVariable UUID bloqueoId) {
    levantarBloqueoUseCase.ejecutar(
        new LevantarBloqueoCommand(id, bloqueoId, SecurityUtils.actual().id()));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/moderacion")
  public ResponseEntity<Void> moderar(@Valid @RequestBody AdminDtos.ModeracionRequest req) {
    moderarContenidoUseCase.ejecutar(
        new ModerarContenidoCommand(
            SecurityUtils.actual().id(),
            req.objetivoTipo(),
            req.objetivoId(),
            req.accion(),
            req.motivo()));
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/verificaciones/{id}/resolver")
  public ResponseEntity<Void> resolverVerificacion(
      @PathVariable UUID id, @Valid @RequestBody AdminDtos.ResolverVerificacionRequest req) {
    resolverVerificacionAdministrativaUseCase.ejecutar(
        new ResolverVerificacionAdministrativaCommand(
            id, SecurityUtils.actual().id(), req.estado(), req.justificacion()));
    return ResponseEntity.noContent().build();
  }
}
