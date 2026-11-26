package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;

/**
 * Interpreta la cabecera {@code Retry-After} de la respuesta de FCM (F09-PUSH-09).
 *
 * <p>Acepta las dos formas del RFC 9110: segundos desde ahora o una fecha HTTP. Una
 * cabecera ausente, vacía o inválida no aporta plazo y el caso de uso aplica solo el
 * backoff con jitter. La cabecera es un dato técnico del proveedor, no un secreto.</p>
 */
public final class RetryAfterFcm {

    private static final String CABECERA = "retry-after";

    private RetryAfterFcm() {
    }

    public static Optional<Instant> interpretar(Map<String, Object> cabeceras, Clock reloj) {
        if (cabeceras == null || cabeceras.isEmpty()) {
            return Optional.empty();
        }
        Object valor = valorDeCabecera(cabeceras);
        if (valor == null) {
            return Optional.empty();
        }
        String texto = valor.toString().trim();
        if (texto.isEmpty()) {
            return Optional.empty();
        }
        try {
            long segundos = Long.parseLong(texto);
            if (segundos < 0) {
                return Optional.empty();
            }
            return Optional.of(reloj.instant().plusSeconds(segundos));
        } catch (NumberFormatException noEsSegundos) {
            return fechaHttp(texto);
        }
    }

    private static Object valorDeCabecera(Map<String, Object> cabeceras) {
        for (Map.Entry<String, Object> entrada : cabeceras.entrySet()) {
            if (entrada.getKey() != null && CABECERA.equalsIgnoreCase(entrada.getKey().trim())) {
                return entrada.getValue();
            }
        }
        return null;
    }

    private static Optional<Instant> fechaHttp(String texto) {
        try {
            return Optional.of(ZonedDateTime.parse(texto, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toInstant());
        } catch (DateTimeParseException noEsFecha) {
            return Optional.empty();
        }
    }
}
