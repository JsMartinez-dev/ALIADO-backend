package com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.usecase.BuscarPrestadoresUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.presentation.mapper.DescubrimientoPresentationMapper;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.usecase.ConsultarPerfilPublicoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.presentation.dto.DescubrimientoDtos;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Descubrimiento de prestadores (SPEC-03). Solo CLIENTE autenticado. */
@RestController
@RequestMapping("/api/v1/descubrimiento")
public class DescubrimientoController {
    private final ConsultarPerfilPublicoUseCase consultarPerfilPublicoUseCase;
    private final BuscarPrestadoresUseCase buscarPrestadoresUseCase;

    public DescubrimientoController(ConsultarPerfilPublicoUseCase consultarPerfilPublicoUseCase,
                                    BuscarPrestadoresUseCase buscarPrestadoresUseCase) {
        this.consultarPerfilPublicoUseCase = consultarPerfilPublicoUseCase;
        this.buscarPrestadoresUseCase = buscarPrestadoresUseCase;
    }

    @GetMapping("/prestadores")
    public ResponseEntity<DescubrimientoDtos.BusquedaResponse> buscar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String zona) {
        SecurityUtils.exigirRol("CLIENTE");
        return ResponseEntity.ok(DescubrimientoPresentationMapper.aResponse(buscarPrestadoresUseCase.ejecutar(
                DescubrimientoPresentationMapper.aQuery(categoria, zona))));
    }

    @GetMapping("/prestadores/{id}/perfil")
    public ResponseEntity<DescubrimientoDtos.PrestadorPublicoResponse> detalle(@PathVariable UUID id) {
        SecurityUtils.exigirRol("CLIENTE");
        return ResponseEntity.ok(DescubrimientoPresentationMapper.aResponse(consultarPerfilPublicoUseCase.ejecutar(id)));
    }
}
