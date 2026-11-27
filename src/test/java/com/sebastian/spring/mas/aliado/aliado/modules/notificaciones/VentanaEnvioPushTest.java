package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.VentanaEnvioPush;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-08/D-08: la ventana de envío exige el timeout del proveedor más el margen para
 * registrar el resultado; con el tiempo exacto se alcanza y por debajo se libera.
 */
class VentanaEnvioPushTest {

    private static final Instant AHORA = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void elTiempoRequeridoSumaElTimeoutYElMargen() {
        assertEquals(Duration.ofSeconds(10),
                new VentanaEnvioPush(Duration.ofSeconds(5), Duration.ofSeconds(5)).requerido());
        assertEquals(Duration.ofSeconds(5),
                new VentanaEnvioPush(Duration.ofSeconds(5), Duration.ZERO).requerido());
    }

    @Test
    void laVentanaSeAlcanzaConElTiempoExactoYNoPorDebajo() {
        VentanaEnvioPush ventana = new VentanaEnvioPush(Duration.ofSeconds(5), Duration.ofSeconds(5));

        assertTrue(ventana.alcanza(AHORA.plusSeconds(10), AHORA));
        assertFalse(ventana.alcanza(AHORA.plusSeconds(10).minusMillis(1), AHORA));
    }

    @Test
    void unaReclamacionVencidaNoAlcanzaAunqueElMargenSeaCero() {
        VentanaEnvioPush ventana = new VentanaEnvioPush(Duration.ofSeconds(5), Duration.ZERO);

        assertFalse(ventana.alcanza(AHORA.minusSeconds(1), AHORA));
        assertFalse(ventana.alcanza(AHORA, AHORA));
    }

    @Test
    void rechazaConfiguracionesInvalidas() {
        assertThrows(IllegalArgumentException.class,
                () -> new VentanaEnvioPush(null, Duration.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> new VentanaEnvioPush(Duration.ZERO, Duration.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> new VentanaEnvioPush(Duration.ofSeconds(-1), Duration.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> new VentanaEnvioPush(Duration.ofSeconds(5), null));
        assertThrows(IllegalArgumentException.class,
                () -> new VentanaEnvioPush(Duration.ofSeconds(5), Duration.ofSeconds(-1)));
    }
}
