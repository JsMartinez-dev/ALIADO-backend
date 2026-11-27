package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.PoliticaReintentoPush;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-08: el backoff es exponencial desde la base, el jitter no reduce el mínimo y
 * el próximo intento es el máximo entre backoff y Retry-After.
 */
class PoliticaReintentoPushTest {

    private static final Instant AHORA = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void elBackoffCreceExponencialmenteDesdeLaBaseSinReducirlaConJitter() {
        PoliticaReintentoPush politica = new PoliticaReintentoPush(5, Duration.ofSeconds(30),
                () -> 0.0);

        assertEquals(Duration.ofSeconds(30), politica.retraso(1));
        assertEquals(Duration.ofSeconds(60), politica.retraso(2));
        assertEquals(Duration.ofSeconds(120), politica.retraso(3));
    }

    @Test
    void elJitterSoloAumentaElRetrasoDentroDeLaBanda() {
        PoliticaReintentoPush politica = new PoliticaReintentoPush(5, Duration.ofSeconds(30),
                () -> 1.0);

        assertEquals(Duration.ofSeconds(45), politica.retraso(1));
        assertEquals(Duration.ofSeconds(90), politica.retraso(2));
    }

    @Test
    void elProximoIntentoEsElMaximoEntreBackoffYRetryAfter() {
        PoliticaReintentoPush politica = new PoliticaReintentoPush(5, Duration.ofSeconds(30),
                () -> 0.0);

        assertEquals(AHORA.plusSeconds(120), politica.proximoIntento(AHORA, 1,
                AHORA.plusSeconds(120)));
        assertEquals(AHORA.plusSeconds(30), politica.proximoIntento(AHORA, 1,
                AHORA.plusSeconds(10)));
        assertEquals(AHORA.plusSeconds(30), politica.proximoIntento(AHORA, 1, null));
    }

    @Test
    void elMaximoDeIntentosIncluyeElIntentoInicial() {
        PoliticaReintentoPush politica = new PoliticaReintentoPush(5, Duration.ofSeconds(30),
                () -> 0.0);

        assertFalse(politica.intentosAgotados(4));
        assertTrue(politica.intentosAgotados(5));
    }
}
