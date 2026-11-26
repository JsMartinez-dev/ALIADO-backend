package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarNotificacionesOportunidadesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarNotificacionesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RegistrarDispositivoPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ResolverOportunidadAvisoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RetirarDispositivoPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RevocarVinculoPushDiferidoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.mapper.NotificacionPresentationMapper;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.dto.NotificacionDtos;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Bandeja de notificaciones propias (SPEC-09) y dispositivos push Android (F09-PUSH-01). */
@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionController {
    private static final String PRUEBA_INSTALACION = "X-ALIADO-Installation-Proof";
    private static final String CREDENCIAL_REVOCACION = "X-ALIADO-Push-Revocation";

    private final ConsultarNotificacionesUseCase notificaciones;
    private final ConsultarNotificacionesOportunidadesUseCase oportunidadesUseCase;
    private final RegistrarDispositivoPushUseCase registrarDispositivo;
    private final RetirarDispositivoPushUseCase retirarDispositivo;
    private final RevocarVinculoPushDiferidoUseCase revocarVinculoDiferido;
    private final ResolverOportunidadAvisoUseCase resolverOportunidadAviso;

    public NotificacionController(ConsultarNotificacionesUseCase notificaciones,
                                  ConsultarNotificacionesOportunidadesUseCase oportunidadesUseCase,
                                  RegistrarDispositivoPushUseCase registrarDispositivo,
                                  RetirarDispositivoPushUseCase retirarDispositivo,
                                  RevocarVinculoPushDiferidoUseCase revocarVinculoDiferido,
                                  ResolverOportunidadAvisoUseCase resolverOportunidadAviso) {
        this.notificaciones = notificaciones;
        this.oportunidadesUseCase = oportunidadesUseCase;
        this.registrarDispositivo = registrarDispositivo;
        this.retirarDispositivo = retirarDispositivo;
        this.revocarVinculoDiferido = revocarVinculoDiferido;
        this.resolverOportunidadAviso = resolverOportunidadAviso;
    }

    @GetMapping
    public ResponseEntity<List<NotificacionDtos.NotificacionResponse>> listar(
            @RequestParam(required = false) String tipo) {
        var command = new com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ConsultarNotificacionesCommand(SecurityUtils.actual().id(), tipo);
        return ResponseEntity.ok(notificaciones.ejecutar(command).stream()
                .map(NotificacionPresentationMapper::aResponse).toList());
    }

    @GetMapping("/oportunidades")
    public ResponseEntity<List<NotificacionDtos.NotificacionResponse>> notificacionesOportunidades() {
        SecurityUtils.exigirRol("PRESTADOR");
        return ResponseEntity.ok(oportunidadesUseCase.ejecutar(SecurityUtils.actual().id()).stream()
                .map(NotificacionPresentationMapper::aResponse).toList());
    }

    /** Estado permitido al destinatario del aviso; cualquier otro caso usa el error genérico. */
    @GetMapping("/{notificacionId}/oportunidad")
    public ResponseEntity<NotificacionDtos.OportunidadDesdeAvisoResponse> oportunidadDesdeAviso(
            @PathVariable UUID notificacionId) {
        SecurityUtils.exigirRol("PRESTADOR");
        return ResponseEntity.ok(NotificacionPresentationMapper.aResponse(
                resolverOportunidadAviso.ejecutar(NotificacionPresentationMapper.aResolverOportunidadCommand(
                        notificacionId, SecurityUtils.actual().id()))));
    }

    /** Alta o sincronización de la instalación Android de la cuenta autenticada. */
    @PutMapping("/dispositivos/{instalacionId}")
    public ResponseEntity<Void> registrarDispositivo(
            @PathVariable UUID instalacionId,
            @RequestHeader(value = PRUEBA_INSTALACION, required = false) String pruebaInstalacion,
            @Valid @RequestBody NotificacionDtos.DispositivoPushRequest request) {
        registrarDispositivo.ejecutar(NotificacionPresentationMapper.aCommand(instalacionId,
                SecurityUtils.actual().id(), pruebaInstalacion, request));
        return ResponseEntity.noContent().build();
    }

    /** Baja en línea al cerrar sesión; revoca solo la generación de la credencial presentada. */
    @DeleteMapping("/dispositivos/{instalacionId}")
    public ResponseEntity<Void> retirarDispositivo(
            @PathVariable UUID instalacionId,
            @RequestHeader(value = PRUEBA_INSTALACION, required = false) String pruebaInstalacion,
            @RequestHeader(value = CREDENCIAL_REVOCACION, required = false) String credencialRevocacion) {
        retirarDispositivo.ejecutar(NotificacionPresentationMapper.aCommand(instalacionId,
                SecurityUtils.actual().id(), pruebaInstalacion, credencialRevocacion));
        return ResponseEntity.noContent().build();
    }

    /** Baja diferida sin sesión, autorizada únicamente por la credencial del vínculo. */
    @PostMapping("/dispositivos/{instalacionId}/revocacion")
    public ResponseEntity<Void> revocarVinculoDiferido(
            @PathVariable UUID instalacionId,
            @RequestHeader(value = CREDENCIAL_REVOCACION, required = false) String credencialRevocacion) {
        revocarVinculoDiferido.ejecutar(NotificacionPresentationMapper.aCommand(instalacionId,
                credencialRevocacion));
        return ResponseEntity.noContent().build();
    }
}
