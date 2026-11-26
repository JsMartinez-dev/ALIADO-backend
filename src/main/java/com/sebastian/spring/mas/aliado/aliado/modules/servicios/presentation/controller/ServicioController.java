package com.sebastian.spring.mas.aliado.aliado.modules.servicios.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.idempotencia.IdempotenciaPort;
import com.sebastian.spring.mas.aliado.aliado.common.web.BaseController;
import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.usecase.*;
import java.util.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.presentation.mapper.ServicioPresentationMapper;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.presentation.dto.ServicioDtos;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ServicioController extends BaseController {
    private final AceptarPropuestaUseCase aceptarPropuestaUseCase;
    private final ConsultarServicioUseCase consultarServicioUseCase;
    private final MarcarServicioEnEjecucionUseCase marcarServicioEnEjecucionUseCase;
    private final MarcarServicioTerminadoUseCase marcarServicioTerminadoUseCase;
    private final ConfirmarFinalizacionServicioUseCase confirmarFinalizacionServicioUseCase;
    private final CancelarServicioUseCase cancelarServicioUseCase;
    private final ConsultarHistorialServicioUseCase consultarHistorialServicioUseCase;
    private final EnviarMensajeServicioUseCase enviarMensajeServicioUseCase;
    private final ConsultarMensajesServicioUseCase consultarMensajesServicioUseCase;

    public ServicioController(AceptarPropuestaUseCase aceptarPropuestaUseCase,
            ConsultarServicioUseCase consultarServicioUseCase,
            MarcarServicioEnEjecucionUseCase marcarServicioEnEjecucionUseCase,
            MarcarServicioTerminadoUseCase marcarServicioTerminadoUseCase,
            ConfirmarFinalizacionServicioUseCase confirmarFinalizacionServicioUseCase,
            CancelarServicioUseCase cancelarServicioUseCase,
            ConsultarHistorialServicioUseCase consultarHistorialServicioUseCase,
            EnviarMensajeServicioUseCase enviarMensajeServicioUseCase,
            ConsultarMensajesServicioUseCase consultarMensajesServicioUseCase,
            ObjectMapper objectMapper, IdempotenciaPort idempotenciaPort) {
        super(objectMapper, idempotenciaPort);
        this.aceptarPropuestaUseCase = aceptarPropuestaUseCase;
        this.consultarServicioUseCase = consultarServicioUseCase;
        this.marcarServicioEnEjecucionUseCase = marcarServicioEnEjecucionUseCase;
        this.marcarServicioTerminadoUseCase = marcarServicioTerminadoUseCase;
        this.confirmarFinalizacionServicioUseCase = confirmarFinalizacionServicioUseCase;
        this.cancelarServicioUseCase = cancelarServicioUseCase;
        this.consultarHistorialServicioUseCase = consultarHistorialServicioUseCase;
        this.enviarMensajeServicioUseCase = enviarMensajeServicioUseCase;
        this.consultarMensajesServicioUseCase = consultarMensajesServicioUseCase;
    }

    @PostMapping("/propuestas/{id}/aceptar")
    public ResponseEntity<?> aceptar(@PathVariable("id") UUID idPropuesta,
            @RequestHeader(value = "Idempotency-Key", required = false) String claveIdempotencia) {
        SecurityUtils.exigirRol("CLIENTE");
        UUID clienteId = SecurityUtils.actual().id();
        return ejecutarIdempotente(claveIdempotencia, "servicios.aceptar." + idPropuesta,
                cuerpo -> clienteId, 200, () -> {
            var servicioCreado = aceptarPropuestaUseCase.ejecutar(
                    ServicioPresentationMapper.aAceptarCommand(idPropuesta, clienteId));
            return ServicioPresentationMapper.aAceptarResponse(servicioCreado);
        });
    }

    @GetMapping("/servicios/{id}")
    public ResponseEntity<ServicioDtos.ServicioResponse> detalle(@PathVariable("id") UUID idServicio) {
        return ResponseEntity.ok(ServicioPresentationMapper.aResponse(consultarServicioUseCase.ejecutar(
                ServicioPresentationMapper.aCommand(idServicio, SecurityUtils.actual().id()))));
    }

    @PostMapping("/servicios/{id}/en-ejecucion")
    public ResponseEntity<?> enEjecucion(@PathVariable("id") UUID idServicio,
            @RequestHeader(value = "Idempotency-Key", required = false) String claveIdempotencia) {
        UUID usuarioId = SecurityUtils.actual().id();
        return ejecutarIdempotente(claveIdempotencia, "servicios." + idServicio + ".en-ejecucion",
                cuerpo -> usuarioId, 200, () -> {
            var servicioActualizado = marcarServicioEnEjecucionUseCase.ejecutar(
                    ServicioPresentationMapper.aCommand(idServicio, usuarioId));
            return ServicioPresentationMapper.aEstadoResponse(servicioActualizado);
        });
    }

    @PostMapping("/servicios/{id}/terminado")
    public ResponseEntity<?> terminado(@PathVariable("id") UUID idServicio,
            @RequestHeader(value = "Idempotency-Key", required = false) String claveIdempotencia) {
        UUID usuarioId = SecurityUtils.actual().id();
        return ejecutarIdempotente(claveIdempotencia, "servicios." + idServicio + ".terminado",
                cuerpo -> usuarioId, 200, () -> {
            var servicioActualizado = marcarServicioTerminadoUseCase.ejecutar(
                    ServicioPresentationMapper.aCommand(idServicio, usuarioId));
            return ServicioPresentationMapper.aEstadoResponse(servicioActualizado);
        });
    }

    @PostMapping("/servicios/{id}/confirmar-finalizacion")
    public ResponseEntity<?> confirmarFinalizacion(@PathVariable("id") UUID idServicio,
            @RequestHeader(value = "Idempotency-Key", required = false) String claveIdempotencia) {
        UUID usuarioId = SecurityUtils.actual().id();
        return ejecutarIdempotente(claveIdempotencia, "servicios." + idServicio + ".confirmar-finalizacion",
                cuerpo -> usuarioId, 200, () -> {
            var servicioActualizado = confirmarFinalizacionServicioUseCase.ejecutar(
                    ServicioPresentationMapper.aCommand(idServicio, usuarioId));
            return ServicioPresentationMapper.aEstadoResponse(servicioActualizado);
        });
    }

    @PostMapping("/servicios/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable("id") UUID idServicio,
            @RequestHeader(value = "Idempotency-Key", required = false) String claveIdempotencia) {
        UUID usuarioId = SecurityUtils.actual().id();
        return ejecutarIdempotente(claveIdempotencia, "servicios." + idServicio + ".cancelar",
                cuerpo -> usuarioId, 200, () -> {
            var servicioCancelado = cancelarServicioUseCase.ejecutar(
                    ServicioPresentationMapper.aCommand(idServicio, usuarioId));
            return ServicioPresentationMapper.aEstadoResponse(servicioCancelado);
        });
    }

    @GetMapping("/servicios/{id}/historial")
    public ResponseEntity<List<ServicioDtos.HistorialResponse>> historial(
            @PathVariable("id") UUID idServicio) {
        return ResponseEntity.ok(consultarHistorialServicioUseCase.ejecutar(
                ServicioPresentationMapper.aCommand(idServicio, SecurityUtils.actual().id())).stream()
                .map(ServicioPresentationMapper::aHistorialResponse).toList());
    }

    @PostMapping("/servicios/{id}/mensajes")
    public ResponseEntity<?> enviarMensaje(@PathVariable("id") UUID idServicio,
            @Valid @RequestBody ServicioDtos.MensajeRequest solicitudMensaje,
            @RequestHeader(value = "Idempotency-Key", required = false) String claveIdempotencia) {
        UUID usuarioId = SecurityUtils.actual().id();
        return ejecutarIdempotente(claveIdempotencia, "servicios." + idServicio + ".mensajes",
                cuerpo -> usuarioId, 201, () -> {
            var mensajeCreado = enviarMensajeServicioUseCase.ejecutar(
                    ServicioPresentationMapper.aMensajeCommand(idServicio, usuarioId, solicitudMensaje));
            return ServicioPresentationMapper.aMensajeResponse(mensajeCreado);
        });
    }

    @GetMapping("/servicios/{id}/mensajes")
    public ResponseEntity<List<ServicioDtos.MensajeResponse>> listarMensajes(
            @PathVariable("id") UUID idServicio) {
        return ResponseEntity.ok(consultarMensajesServicioUseCase.ejecutar(
                ServicioPresentationMapper.aCommand(idServicio, SecurityUtils.actual().id())).stream()
                .map(ServicioPresentationMapper::aMensajeResponse).toList());
    }
}
