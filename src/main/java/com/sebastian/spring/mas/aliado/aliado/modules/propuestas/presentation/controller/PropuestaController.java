package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.idempotencia.IdempotenciaPort;
import com.sebastian.spring.mas.aliado.aliado.common.web.BaseController;
import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.usecase.ListarPropuestasRecibidasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto.ListadoPropuestasQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.presentation.mapper.PropuestaPresentationMapper;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model.Propuesta;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.presentation.dto.PropuestaDtos;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PropuestaController extends BaseController {
    private final ListarPropuestasRecibidasUseCase listarPropuestas;
    private final CrearPropuestaUseCase crearPropuesta;
    private final EditarPropuestaUseCase editarPropuesta;
    private final RetirarPropuestaUseCase retirarPropuesta;

    public PropuestaController(ListarPropuestasRecibidasUseCase listarPropuestas, CrearPropuestaUseCase crearPropuesta,
                               EditarPropuestaUseCase editarPropuesta, RetirarPropuestaUseCase retirarPropuesta, ObjectMapper objectMapper,
                               IdempotenciaPort idempotencyService) {
        super(objectMapper, idempotencyService);
        this.listarPropuestas = listarPropuestas;
        this.crearPropuesta = crearPropuesta;
        this.editarPropuesta = editarPropuesta;
        this.retirarPropuesta = retirarPropuesta;
    }

    @PostMapping("/propuestas")
    public ResponseEntity<?> crear(@Valid @RequestBody PropuestaDtos.CrearRequest req,
                                   @RequestHeader(value = "Idempotency-Key", required = false) String claveHeader) {
        SecurityUtils.exigirRol("PRESTADOR");
        UUID prestadorId = SecurityUtils.actual().id();
        return ejecutarIdempotente(claveHeader, "propuestas.crear", cuerpo -> prestadorId, 201, () -> {
            var creada = crearPropuesta.ejecutar(PropuestaPresentationMapper.aCommand(prestadorId, req));
            return PropuestaPresentationMapper.aResponse(creada);
        });
    }

    @PutMapping("/propuestas/{id}")
    public ResponseEntity<PropuestaDtos.PropuestaResponse> editar(@PathVariable UUID id,
            @Valid @RequestBody PropuestaDtos.EditarRequest req) {
        SecurityUtils.exigirRol("PRESTADOR");
        return ResponseEntity.ok(PropuestaPresentationMapper.aResponse(editarPropuesta.ejecutar(
                PropuestaPresentationMapper.aEditarCommand(id, SecurityUtils.actual().id(), req))));
    }

    @PostMapping("/propuestas/{id}/retirar")
    public ResponseEntity<PropuestaDtos.IdEstadoResponse> retirar(@PathVariable UUID id) {
        SecurityUtils.exigirRol("PRESTADOR");
        var retirada = retirarPropuesta.ejecutar(PropuestaPresentationMapper.aRetirarCommand(id, SecurityUtils.actual().id()));
        return ResponseEntity.ok(PropuestaPresentationMapper.aEstadoResponse(retirada));
    }

    @GetMapping("/solicitudes/{solicitudId}/propuestas")
    public ResponseEntity<List<PropuestaDtos.PropuestaResponse>> propuestasDeSolicitud(
            @PathVariable UUID solicitudId) {
        SecurityUtils.exigirRol("CLIENTE");
        return ResponseEntity.ok(listarPropuestas.ejecutar(new ListadoPropuestasQuery(solicitudId, SecurityUtils.actual().id())).stream()
                .map(PropuestaPresentationMapper::aResponse).toList());
    }
}
