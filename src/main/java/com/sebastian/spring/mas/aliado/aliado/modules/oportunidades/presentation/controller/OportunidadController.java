package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.ConsultarDetalleOportunidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.ConsultarTableroOportunidadesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.presentation.mapper.OportunidadPresentationMapper;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.presentation.dto.OportunidadDtos;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class OportunidadController {
    private final ConsultarDetalleOportunidadUseCase detalleUseCase;
    private final ConsultarTableroOportunidadesUseCase tableroUseCase;

    public OportunidadController(ConsultarDetalleOportunidadUseCase detalleUseCase, ConsultarTableroOportunidadesUseCase tableroUseCase) {
        this.detalleUseCase = detalleUseCase;
        this.tableroUseCase = tableroUseCase;
    }

    @GetMapping("/oportunidades")
    public ResponseEntity<List<OportunidadDtos.OportunidadResponse>> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String urgencia) {
        SecurityUtils.exigirRol("PRESTADOR");
        return ResponseEntity.ok(tableroUseCase.ejecutar(
                OportunidadPresentationMapper.aQuery(SecurityUtils.actual().id(), categoria, urgencia)).stream()
                .map(OportunidadPresentationMapper::aResponse).toList());
    }

    @GetMapping("/oportunidades/{solicitudId}")
    public ResponseEntity<OportunidadDtos.OportunidadResponse> detalle(@PathVariable UUID solicitudId) {
        SecurityUtils.exigirRol("PRESTADOR");
        return ResponseEntity.ok(OportunidadPresentationMapper.aResponse(detalleUseCase.ejecutar(
                OportunidadPresentationMapper.aDetalleQuery(solicitudId, SecurityUtils.actual().id()))));
    }
}
