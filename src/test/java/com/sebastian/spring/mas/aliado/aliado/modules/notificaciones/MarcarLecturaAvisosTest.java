package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.MarcarAvisoLeidoCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.MarcarTodasLeidasCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.MarcarAvisoLeidoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.MarcarTodasLeidasUseCase;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * F09-BANDEJA-02 sobre el contexto Spring y la base de la suite: el puerto ampliado y sus
 * dos casos de uso comparten las reglas de pertenencia, primera fecha y lectura masiva. La
 * concurrencia real y la semántica transaccional de PostgreSQL se validan aparte, en
 * {@link MarcarLecturaAvisosPostgresTest}.
 */
@Timeout(60)
@SpringBootTest
class MarcarLecturaAvisosTest {

    private static final OffsetDateTime GENERADA =
            OffsetDateTime.of(2026, 3, 1, 9, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime ENTREGADA_EN =
            OffsetDateTime.of(2026, 3, 1, 9, 1, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime LEIDA_ANTES =
            OffsetDateTime.of(2026, 3, 1, 10, 0, 0, 0, ZoneOffset.UTC);

    @Autowired private MarcarAvisoLeidoUseCase marcarUno;
    @Autowired private MarcarTodasLeidasUseCase marcarTodas;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void limpiar() {
        jdbc.update("DELETE FROM notificaciones");
    }

    @Test
    void marcarUnAvisoPropioRegistraLaHoraDelServidorYConservaLaEntrega() {
        UUID cuenta = UUID.randomUUID();
        UUID aviso = sembrarAviso(cuenta, "ENTREGADA", 2, ENTREGADA_EN, null);

        OffsetDateTime antes = OffsetDateTime.now(ZoneOffset.UTC);
        marcarUno.ejecutar(new MarcarAvisoLeidoCommand(aviso, cuenta));
        OffsetDateTime despues = OffsetDateTime.now(ZoneOffset.UTC);

        OffsetDateTime leida = leidaEn(aviso);
        assertNotNull(leida, "el aviso propio pendiente queda leído");
        assertFalse(leida.isBefore(antes.minusSeconds(1)), "la fecha es la hora del servidor");
        assertFalse(leida.isAfter(despues.plusSeconds(1)), "la fecha es la hora del servidor");

        Map<String, Object> fila = jdbc.queryForMap(
                "SELECT estado_entrega, intentos, fecha_entrega FROM notificaciones WHERE id = ?",
                aviso);
        assertEquals("ENTREGADA", fila.get("estado_entrega"),
                "la lectura no modifica el estado de entrega");
        assertEquals(2, ((Number) fila.get("intentos")).intValue(),
                "la lectura no modifica los intentos");
        assertNotNull(fila.get("fecha_entrega"), "la lectura no modifica la fecha de entrega");
    }

    @Test
    void repetirLaLecturaConservaLaPrimeraFecha() {
        UUID cuenta = UUID.randomUUID();
        UUID aviso = sembrarAviso(cuenta, "PENDIENTE", 0, null, null);

        marcarUno.ejecutar(new MarcarAvisoLeidoCommand(aviso, cuenta));
        OffsetDateTime primera = leidaEn(aviso);
        assertNotNull(primera);

        assertDoesNotThrow(() -> marcarUno.ejecutar(new MarcarAvisoLeidoCommand(aviso, cuenta)));

        assertEquals(primera.toInstant(), leidaEn(aviso).toInstant(),
                "repetir la operación conserva la primera fecha");
    }

    @Test
    void unAvisoYaLeidoSigueSiendoPropioYNoCambiaSuFecha() {
        UUID cuenta = UUID.randomUUID();
        UUID aviso = sembrarAviso(cuenta, "ENTREGADA", 1, ENTREGADA_EN, LEIDA_ANTES);

        assertDoesNotThrow(() -> marcarUno.ejecutar(new MarcarAvisoLeidoCommand(aviso, cuenta)));

        assertEquals(LEIDA_ANTES.toInstant(), leidaEn(aviso).toInstant());
    }

    @Test
    void avisoAjenoEInexistenteProducenElMismoRecursoNoEncontrado() {
        UUID cuenta = UUID.randomUUID();
        UUID ajeno = sembrarAviso(UUID.randomUUID(), "PENDIENTE", 0, null, null);

        BusinessException ajenoEx = assertThrows(BusinessException.class,
                () -> marcarUno.ejecutar(new MarcarAvisoLeidoCommand(ajeno, cuenta)));
        BusinessException inexistenteEx = assertThrows(BusinessException.class,
                () -> marcarUno.ejecutar(new MarcarAvisoLeidoCommand(UUID.randomUUID(), cuenta)));

        assertEquals("NOTIFICACION_NO_ENCONTRADA", ajenoEx.code());
        assertEquals(404, ajenoEx.status());
        assertEquals(ajenoEx.code(), inexistenteEx.code());
        assertEquals(ajenoEx.status(), inexistenteEx.status());
        assertEquals(ajenoEx.getMessage(), inexistenteEx.getMessage());
        assertNull(leidaEn(ajeno), "el aviso de otra cuenta permanece intacto");
    }

    @Test
    void marcarTodasAfectaSoloPendientesDeLaCuentaSolicitante() {
        UUID cuenta = UUID.randomUUID();
        UUID otra = UUID.randomUUID();
        UUID pendienteUno = sembrarAviso(cuenta, "PENDIENTE", 0, null, null);
        UUID pendienteDos = sembrarAviso(cuenta, "PENDIENTE", 0, null, null);
        UUID yaLeida = sembrarAviso(cuenta, "ENTREGADA", 1, ENTREGADA_EN, LEIDA_ANTES);
        UUID ajena = sembrarAviso(otra, "PENDIENTE", 0, null, null);

        marcarTodas.ejecutar(new MarcarTodasLeidasCommand(cuenta));

        assertNotNull(leidaEn(pendienteUno));
        assertNotNull(leidaEn(pendienteDos));
        assertEquals(LEIDA_ANTES.toInstant(), leidaEn(yaLeida).toInstant(),
                "una lectura anterior no se sustituye");
        assertNull(leidaEn(ajena), "los avisos de otra cuenta no se tocan");

        Map<String, Object> fila = jdbc.queryForMap(
                "SELECT estado_entrega, intentos, fecha_entrega FROM notificaciones WHERE id = ?",
                pendienteUno);
        assertEquals("PENDIENTE", fila.get("estado_entrega"));
        assertEquals(0, ((Number) fila.get("intentos")).intValue());
        assertNull(fila.get("fecha_entrega"));

        // Un aviso publicado después de la actualización no queda leído por ella.
        UUID publicadaDespues = sembrarAviso(cuenta, "PENDIENTE", 0, null, null);
        assertNull(leidaEn(publicadaDespues));

        // Repetir y una cuenta sin pendientes terminan con éxito.
        assertDoesNotThrow(() -> marcarTodas.ejecutar(new MarcarTodasLeidasCommand(cuenta)));
        assertNotNull(leidaEn(publicadaDespues));
        assertDoesNotThrow(() -> marcarTodas.ejecutar(
                new MarcarTodasLeidasCommand(UUID.randomUUID())));
    }

    private OffsetDateTime leidaEn(UUID aviso) {
        return jdbc.queryForObject("SELECT leida_en FROM notificaciones WHERE id = ?",
                (rs, fila) -> rs.getObject("leida_en", OffsetDateTime.class), aviso);
    }

    private UUID sembrarAviso(UUID destinatario, String estadoEntrega, int intentos,
                              OffsetDateTime fechaEntrega, OffsetDateTime leidaEn) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO notificaciones (id, destinatario_id, tipo, aggregate_id, event_version,
                                            detalle, canal, estado_entrega, intentos, fecha_generacion,
                                            fecha_entrega, leida_en)
                VALUES (?, ?, 'NUEVA_OPORTUNIDAD', ?, 1, 'detalle', 'PUSH', ?, ?, ?, ?, ?)
                """, id, destinatario, UUID.randomUUID(), estadoEntrega, intentos, GENERADA,
                fechaEntrega, leidaEn);
        return id;
    }
}
