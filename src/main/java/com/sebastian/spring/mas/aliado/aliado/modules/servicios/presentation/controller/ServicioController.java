package com.sebastian.spring.mas.aliado.aliado.modules.servicios.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.idempotencia.IdempotenciaPort;
import com.sebastian.spring.mas.aliado.aliado.common.web.BaseController;
import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.usecase.*;
import java.util.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/** Contratación y ciclo de vida del servicio (SPEC-08). */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Servicios", description = "SPEC-08. Operaciones autenticadas; el acceso se limita a participantes.")
public class ServicioController extends BaseController {
    private final AceptarPropuestaUseCase aceptarPropuesta;
    private final ConsultarServicioUseCase consultarServicio;
    private final ConsultarServiciosPropiosUseCase consultarServiciosPropios;
    private final MarcarServicioEnEjecucionUseCase enEjecucion;
    private final MarcarServicioTerminadoUseCase terminado;
    private final ConfirmarFinalizacionServicioUseCase confirmarFinalizacion;
    private final CancelarServicioUseCase cancelar;
    private final ConsultarHistorialServicioUseCase historial;
    private final EnviarMensajeServicioUseCase enviarMensaje;
    private final ConsultarMensajesServicioUseCase listarMensajes;
    public ServicioController(AceptarPropuestaUseCase aceptarPropuesta, ConsultarServicioUseCase consultarServicio, ConsultarServiciosPropiosUseCase consultarServiciosPropios, MarcarServicioEnEjecucionUseCase enEjecucion, MarcarServicioTerminadoUseCase terminado, ConfirmarFinalizacionServicioUseCase confirmarFinalizacion, CancelarServicioUseCase cancelar, ConsultarHistorialServicioUseCase historial, EnviarMensajeServicioUseCase enviarMensaje, ConsultarMensajesServicioUseCase listarMensajes, ObjectMapper objectMapper, IdempotenciaPort idempotencyService){
        super(objectMapper,idempotencyService);
        this.aceptarPropuesta=aceptarPropuesta;
        this.consultarServicio=consultarServicio;
        this.consultarServiciosPropios=consultarServiciosPropios;
        this.enEjecucion=enEjecucion;
        this.terminado=terminado;
        this.confirmarFinalizacion=confirmarFinalizacion;
        this.cancelar=cancelar;
        this.historial=historial;
        this.enviarMensaje=enviarMensaje;
        this.listarMensajes=listarMensajes;
    }

    @PostMapping("/propuestas/{id}/aceptar")
    public ResponseEntity<?> aceptar(@PathVariable UUID id,
                                     @RequestHeader(value = "Idempotency-Key", required = false) String claveHeader) {
        SecurityUtils.exigirRol("CLIENTE");
        UUID clienteId = SecurityUtils.actual().id();
        return ejecutarIdempotente(claveHeader, "servicios.aceptar." + id, cuerpo -> clienteId, 200, () -> {
            var servicio = aceptarPropuesta.ejecutar(ServicioPresentationMapper.aAceptarCommand(id,clienteId));
            return ServicioPresentationMapper.aAceptarResponse(servicio);
        });
    }

    @GetMapping("/servicios/mios")
    @Operation(summary = "Listar servicios propios",
            description = "Requiere sesión. Devuelve solo servicios donde el usuario es cliente o prestador. "
                    + "Orden descendente por fechaContratacion con desempate por id.")
    public ResponseEntity<ServicioDtos.ServiciosPropiosPage> misServicios(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int pagina,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int tamano) {
        if (pagina < 0 || tamano < 1 || tamano > 100) {
            throw new com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException.BadRequest(
                    "PAGINACION_INVALIDA", "pagina debe ser >= 0 y tamano debe estar entre 1 y 100.");
        }
        var resultado = consultarServiciosPropios.ejecutar(SecurityUtils.actual().id(), pagina, tamano);
        return ResponseEntity.ok(new ServicioDtos.ServiciosPropiosPage(
                resultado.items().stream().map(ServicioPresentationMapper::aResumenResponse).toList(),
                resultado.pagina(), resultado.tamano(), resultado.totalElementos(), resultado.totalPaginas()));
    }

    @GetMapping("/servicios/{id}")
    public ResponseEntity<ServicioDtos.ServicioResponse> detalle(@PathVariable UUID id) {
        return ResponseEntity.ok(ServicioPresentationMapper.aResponse(consultarServicio.ejecutar(ServicioPresentationMapper.aCommand(id, SecurityUtils.actual().id()))));
    }

    @PostMapping("/servicios/{id}/en-ejecucion")
    public ResponseEntity<?> enEjecucion(@PathVariable UUID id,
            @RequestHeader(value = "Idempotency-Key", required = false) String claveHeader) {
        UUID usuarioId = SecurityUtils.actual().id();
        return ejecutarIdempotente(claveHeader, "servicios." + id + ".en-ejecucion", cuerpo -> usuarioId, 200, () -> {
            var s = enEjecucion.ejecutar(ServicioPresentationMapper.aCommand(id, usuarioId));
            return ServicioPresentationMapper.aEstadoResponse(s);
        });
    }

    @PostMapping("/servicios/{id}/terminado")
    public ResponseEntity<?> terminado(@PathVariable UUID id,
            @RequestHeader(value = "Idempotency-Key", required = false) String claveHeader) {
        UUID usuarioId = SecurityUtils.actual().id();
        return ejecutarIdempotente(claveHeader, "servicios." + id + ".terminado", cuerpo -> usuarioId, 200, () -> {
            var s = terminado.ejecutar(ServicioPresentationMapper.aCommand(id, usuarioId));
            return ServicioPresentationMapper.aEstadoResponse(s);
        });
    }

    @PostMapping("/servicios/{id}/confirmar-finalizacion")
    public ResponseEntity<?> confirmarFinalizacion(@PathVariable UUID id,
            @RequestHeader(value = "Idempotency-Key", required = false) String claveHeader) {
        UUID usuarioId = SecurityUtils.actual().id();
        return ejecutarIdempotente(claveHeader, "servicios." + id + ".confirmar-finalizacion",
                cuerpo -> usuarioId, 200, () -> {
            var s = confirmarFinalizacion.ejecutar(ServicioPresentationMapper.aCommand(id, usuarioId));
            return ServicioPresentationMapper.aEstadoResponse(s);
        });
    }

    @PostMapping("/servicios/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable UUID id,
            @RequestHeader(value = "Idempotency-Key", required = false) String claveHeader) {
        UUID usuarioId = SecurityUtils.actual().id();
        return ejecutarIdempotente(claveHeader, "servicios." + id + ".cancelar", cuerpo -> usuarioId, 200, () -> {
            var s = cancelar.ejecutar(ServicioPresentationMapper.aCommand(id, usuarioId));
            return ServicioPresentationMapper.aEstadoResponse(s);
        });
    }

    @GetMapping("/servicios/{id}/historial")
    public ResponseEntity<List<ServicioDtos.HistorialResponse>> historial(@PathVariable UUID id) {
        return ResponseEntity.ok(historial.ejecutar(ServicioPresentationMapper.aCommand(id,SecurityUtils.actual().id())).stream().map(ServicioPresentationMapper::aHistorialResponse).toList());
    }

    @PostMapping("/servicios/{id}/mensajes")
    public ResponseEntity<?> enviarMensaje(@PathVariable UUID id,
            @Valid @RequestBody ServicioDtos.MensajeRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String claveHeader) {
        UUID usuarioId = SecurityUtils.actual().id();
        return ejecutarIdempotente(claveHeader, "servicios." + id + ".mensajes", cuerpo -> usuarioId, 201, () -> {
            var m = enviarMensaje.ejecutar(ServicioPresentationMapper.aMensajeCommand(id,usuarioId,req));
            return ServicioPresentationMapper.aMensajeResponse(m);
        });
    }

    @GetMapping("/servicios/{id}/mensajes")
    public ResponseEntity<List<ServicioDtos.MensajeResponse>> listarMensajes(@PathVariable UUID id) {
        return ResponseEntity.ok(listarMensajes.ejecutar(ServicioPresentationMapper.aCommand(id,SecurityUtils.actual().id())).stream().map(ServicioPresentationMapper::aMensajeResponse).toList());
    }
}
