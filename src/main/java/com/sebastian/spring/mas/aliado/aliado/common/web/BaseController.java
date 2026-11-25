package com.sebastian.spring.mas.aliado.aliado.common.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sebastian.spring.mas.aliado.aliado.common.idempotencia.IdempotenciaPort;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.http.ResponseEntity;


public abstract class BaseController {
    protected final ObjectMapper objectMapper;
    protected final IdempotenciaPort idempotencyService;

    protected BaseController(ObjectMapper objectMapper, IdempotenciaPort idempotencyService) {
        this.objectMapper = objectMapper;
        this.idempotencyService = idempotencyService;
    }

    protected Optional<ResponseEntity<JsonNode>> respuestaGuardada(UUID clave, String operacion) {
        return idempotencyService.buscar(clave, operacion)
                .map(guardada -> ResponseEntity.status(guardada.statusCode()).body(guardada.cuerpo()));
    }

    protected void guardarRespuesta(UUID clave, String operacion, UUID usuarioId, int status, Object cuerpo) {
        try {
            JsonNode nodo = objectMapper.valueToTree(cuerpo);
            idempotencyService.guardar(clave, operacion, usuarioId, status, nodo);
        } catch (IllegalArgumentException ignored) {
            // La idempotencia nunca debe romper la operación principal.
        }
    }

    protected static UUID claveIdempotencia(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(header.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }


    protected <T> ResponseEntity<?> ejecutarIdempotente(String claveHeader, String operacion,
                                                        Function<T, UUID> usuarioDe, int statusExito,
                                                        Supplier<T> accion) {
        UUID clave = claveIdempotencia(claveHeader);
        if (clave != null) {
            var guardada = respuestaGuardada(clave, operacion);
            if (guardada.isPresent()) {
                return guardada.get();
            }
        }
        T cuerpo = accion.get();
        guardarRespuesta(clave, operacion, usuarioDe.apply(cuerpo), statusExito, cuerpo);
        return ResponseEntity.status(statusExito).body(cuerpo);
    }
}
