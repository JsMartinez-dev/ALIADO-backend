package com.sebastian.spring.mas.aliado.aliado.common.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sebastian.spring.mas.aliado.aliado.common.idempotencia.IdempotenciaPort;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Utilidades compartidas por los controladores de escritura.
 * La idempotencia (plan general, sección 14) se apoya en la cabecera
 * Idempotency-Key: si la clave ya fue procesada para la operación,
 * se devuelve la respuesta guardada sin ejecutar de nuevo el comando.
 */
public abstract class BaseController {
    protected final ObjectMapper objectMapper;
    protected final IdempotenciaPort idempotencyService;

    protected BaseController(ObjectMapper objectMapper, IdempotenciaPort idempotencyService) {
        this.objectMapper = objectMapper;
        this.idempotencyService = idempotencyService;
    }

    protected Optional<ResponseEntity<String>> respuestaGuardada(UUID clave, String operacion) {
        return idempotencyService.buscar(clave, operacion)
                .map(guardada -> ResponseEntity.status(guardada.statusCode())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(guardada.cuerpo().toString()));
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

    /**
     * Plantilla de comando idempotente: busca la respuesta guardada para la clave,
     * ejecuta la acción solo si no existe y guarda el resultado. El usuario dueño
     * de la respuesta se extrae del cuerpo, porque en algunos comandos (registro)
     * solo se conoce después de ejecutar.
     */
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
