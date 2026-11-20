package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarNotificacionesOportunidadesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.mapper.NotificacionPresentationMapper;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.dto.NotificacionDtos;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Bandeja de notificaciones propias (SPEC-09). Sin endpoint de creación. */
@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionController {
    private final com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarNotificacionesUseCase notificaciones;
    private final ConsultarNotificacionesOportunidadesUseCase oportunidadesUseCase;

    public NotificacionController(com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarNotificacionesUseCase notificaciones,
                                  ConsultarNotificacionesOportunidadesUseCase oportunidadesUseCase) {
        this.notificaciones = notificaciones;
        this.oportunidadesUseCase = oportunidadesUseCase;
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
}
