package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm.RetryAfterFcm;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** F09-PUSH-09: lectura de {@code Retry-After} de la respuesta de FCM. */
class RetryAfterFcmTest {

    private static final Instant AHORA = Instant.parse("2026-03-01T10:00:00Z");
    private static final Clock RELOJ = Clock.fixed(AHORA, ZoneOffset.UTC);

    @Test
    void interpretaSegundosDesdeAhora() {
        assertEquals(Optional.of(AHORA.plusSeconds(30)),
                RetryAfterFcm.interpretar(Map.of("Retry-After", "30"), RELOJ));
    }

    @Test
    void interpretaLaFechaHttpSinDistinguirMayusculasEnLaCabecera() {
        assertEquals(Optional.of(AHORA.plusSeconds(45)),
                RetryAfterFcm.interpretar(
                        Map.of("retry-after", "Sun, 1 Mar 2026 10:00:45 GMT"), RELOJ));
    }

    @Test
    void unaCabeceraAusenteVaciaONoValidaNoAportaPlazo() {
        assertTrue(RetryAfterFcm.interpretar(null, RELOJ).isEmpty());
        assertTrue(RetryAfterFcm.interpretar(Map.of(), RELOJ).isEmpty());
        assertTrue(RetryAfterFcm.interpretar(Map.of("Retry-After", ""), RELOJ).isEmpty());
        assertTrue(RetryAfterFcm.interpretar(Map.of("Retry-After", "-5"), RELOJ).isEmpty());
        assertTrue(RetryAfterFcm.interpretar(Map.of("Retry-After", "pronto"), RELOJ).isEmpty());
        assertTrue(RetryAfterFcm.interpretar(Map.of("otra", "30"), RELOJ).isEmpty());
    }
}
