package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.RetrocesoReconexionPush;
import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** F09-PUSH-06: retroceso exponencial con jitter acotado por el máximo configurado. */
class RetrocesoReconexionPushTest {

    @Test
    void duplicaElRetrasoYSeReiniciaTrasConfirmarLaSuscripcion() {
        RetrocesoReconexionPush retroceso = new RetrocesoReconexionPush(
                Duration.ofMillis(100), Duration.ofSeconds(5), () -> 0.0);

        assertEquals(Duration.ofMillis(50), retroceso.siguiente());
        assertEquals(Duration.ofMillis(100), retroceso.siguiente());
        assertEquals(Duration.ofMillis(200), retroceso.siguiente());

        retroceso.reiniciar();

        assertEquals(Duration.ofMillis(50), retroceso.siguiente());
    }

    @Test
    void elJitterDispersaSinSuperarElMaximo() {
        RetrocesoReconexionPush alMinimo = new RetrocesoReconexionPush(
                Duration.ofMillis(100), Duration.ofMillis(300), () -> 0.0);
        RetrocesoReconexionPush alMaximo = new RetrocesoReconexionPush(
                Duration.ofMillis(100), Duration.ofMillis(300), () -> 0.9999);

        Duration primerMinimo = alMinimo.siguiente();
        Duration primerMaximo = alMaximo.siguiente();
        assertEquals(Duration.ofMillis(50), primerMinimo);
        assertTrue(primerMaximo.toMillis() > primerMinimo.toMillis(),
                "el jitter debe dispersar los reintentos: " + primerMaximo);

        for (int intento = 1; intento < 6; intento++) {
            Duration minimo = alMinimo.siguiente();
            Duration maximo = alMaximo.siguiente();
            assertTrue(minimo.toMillis() >= 1, "el retraso siempre debe ser positivo");
            assertTrue(maximo.toMillis() <= 300, "el retraso no supera el máximo: " + maximo);
            assertTrue(maximo.compareTo(minimo) >= 0, "el jitter nunca queda por debajo del mínimo");
        }
    }

    @Test
    void rechazaUnaConfiguracionInvalida() {
        assertThrows(IllegalArgumentException.class,
                () -> new RetrocesoReconexionPush(Duration.ZERO, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class,
                () -> new RetrocesoReconexionPush(Duration.ofSeconds(2), Duration.ofSeconds(1)));
    }
}
