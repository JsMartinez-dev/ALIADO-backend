package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.cursor.CursorBandeja;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoLectura;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Formato del cursor de bandeja (F09-BANDEJA-03): conserva la precisión de la fecha y el
 * UUID, y rechaza versión, formato o filtros distintos con el mismo error controlado.
 */
class CursorBandejaTest {

    private static final OffsetDateTime FECHA =
            OffsetDateTime.of(2026, 3, 1, 9, 0, 0, 123_456_000, ZoneOffset.UTC);
    private static final UUID ID = UUID.fromString("0f0e0d0c-0b0a-4908-8706-050403020100");

    @Test
    void conservaLaPrecisionDeLaFechaYElUuid() {
        String cursor = CursorBandeja.codificar(FECHA, ID, EstadoLectura.NO_LEIDA,
                TipoNotificacion.NUEVA_OPORTUNIDAD);

        CursorBandeja decodificado = CursorBandeja.decodificar(cursor, EstadoLectura.NO_LEIDA,
                TipoNotificacion.NUEVA_OPORTUNIDAD);

        assertEquals(FECHA.toInstant(), decodificado.fechaGeneracion().toInstant());
        assertEquals(FECHA.getNano(), decodificado.fechaGeneracion().getNano());
        assertEquals(ID, decodificado.id());
    }

    @Test
    void unCursorDeOtrosFiltrosSeRechaza() {
        String cursor = CursorBandeja.codificar(FECHA, ID, EstadoLectura.LEIDA,
                TipoNotificacion.PROPUESTA_RECIBIDA);

        assertCursorInvalido(() -> CursorBandeja.decodificar(cursor, EstadoLectura.NO_LEIDA,
                TipoNotificacion.PROPUESTA_RECIBIDA));
        assertCursorInvalido(() -> CursorBandeja.decodificar(cursor, EstadoLectura.LEIDA,
                TipoNotificacion.NUEVA_OPORTUNIDAD));
    }

    @Test
    void unCursorDeVersionDesconocidaSeRechaza() {
        String plano = "v2|" + FECHA.toEpochSecond() + "|" + FECHA.getNano() + "|" + ID
                + "|" + EstadoLectura.NO_LEIDA.name() + ":";
        String cursor = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(plano.getBytes(StandardCharsets.UTF_8));

        assertCursorInvalido(() -> CursorBandeja.decodificar(cursor, EstadoLectura.NO_LEIDA, null));
    }

    @Test
    void unCursorMalFormadoSeRechaza() {
        assertCursorInvalido(() -> CursorBandeja.decodificar(null, EstadoLectura.NO_LEIDA, null));
        assertCursorInvalido(() -> CursorBandeja.decodificar("   ", EstadoLectura.NO_LEIDA, null));
        assertCursorInvalido(() -> CursorBandeja.decodificar("%%%", EstadoLectura.NO_LEIDA, null));
        assertCursorInvalido(() -> CursorBandeja.decodificar("a".repeat(513),
                EstadoLectura.NO_LEIDA, null));
        String tresPartes = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("v1|1|0".getBytes(StandardCharsets.UTF_8));
        assertCursorInvalido(() -> CursorBandeja.decodificar(tresPartes, EstadoLectura.NO_LEIDA, null));
        String fechaFueraDeRango = Base64.getUrlEncoder().withoutPadding().encodeToString(
                ("v1|" + Long.MAX_VALUE + "|0|" + ID + "|" + EstadoLectura.NO_LEIDA.name() + ":")
                        .getBytes(StandardCharsets.UTF_8));
        assertCursorInvalido(() -> CursorBandeja.decodificar(fechaFueraDeRango,
                EstadoLectura.NO_LEIDA, null));
    }

    private static void assertCursorInvalido(org.junit.jupiter.api.function.Executable ejecutable) {
        BusinessException error = assertThrows(BusinessException.class, ejecutable);
        assertEquals("CURSOR_INVALIDO", error.code());
        assertEquals(400, error.status());
    }
}
