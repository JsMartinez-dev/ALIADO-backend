package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.MarcarAvisoLeidoCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.MarcarTodasLeidasCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.MarcarAvisoLeidoService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.MarcarTodasLeidasService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.MarcarAvisoLeidoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.MarcarTodasLeidasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-BANDEJA-02 sobre PostgreSQL real: dos escrituras concurrentes de la misma cuenta no
 * sustituyen la fecha de la primera lectura, un aviso ajeno y uno inexistente comparten el
 * recurso no encontrado y la lectura masiva alcanza solo los pendientes de la cuenta sin
 * tocar estados, intentos ni trabajos push.
 *
 * <p>Se ejecuta solo con {@code ALIADO_PG_URL} definido y una base desechable; el
 * comportamiento de bloqueo y la semántica del {@code UPDATE} no se pueden validar en H2.</p>
 */
@EnabledIfEnvironmentVariable(named = "ALIADO_PG_URL", matches = ".+")
@Timeout(120)
@SpringBootTest
class MarcarLecturaAvisosPostgresTest {

    private static final OffsetDateTime GENERADA =
            OffsetDateTime.of(2026, 3, 1, 9, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime ENTREGADA_EN =
            OffsetDateTime.of(2026, 3, 1, 9, 1, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime LEIDA_ANTES =
            OffsetDateTime.of(2026, 3, 1, 10, 0, 0, 0, ZoneOffset.UTC);

    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getenv("ALIADO_PG_URL"));
        registry.add("spring.datasource.username", () -> leer("ALIADO_PG_USERNAME", "aliado"));
        registry.add("spring.datasource.password", () -> leer("ALIADO_PG_PASSWORD", "aliado"));
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired private JdbcTemplate jdbc;
    @Autowired private NotificacionRepository notificaciones;
    @Autowired private PlatformTransactionManager transactionManager;

    @BeforeEach
    void limpiar() {
        jdbc.update("DELETE FROM entregas_push");
        jdbc.update("DELETE FROM trabajos_push");
        jdbc.update("DELETE FROM vinculos_push");
        jdbc.update("DELETE FROM instalaciones_push");
        jdbc.update("DELETE FROM notificaciones");
    }

    @Test
    void dosEscriturasConcurrentesConservanLaPrimeraFecha() throws Exception {
        UUID cuenta = sembrarUsuario();
        UUID aviso = sembrarAviso(cuenta, "ENTREGADA", 2, ENTREGADA_EN, null);
        OffsetDateTime primera = LEIDA_ANTES;
        OffsetDateTime segunda = LEIDA_ANTES.plusMinutes(5);

        MarcarAvisoLeidoUseCase uno = servicioIndividual(primera);
        MarcarAvisoLeidoUseCase dos = servicioIndividual(segunda);
        CyclicBarrier barrera = new CyclicBarrier(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> escrituraUno = pool.submit(() -> {
                barrera.await(5, TimeUnit.SECONDS);
                uno.ejecutar(new MarcarAvisoLeidoCommand(aviso, cuenta));
                return null;
            });
            Future<?> escrituraDos = pool.submit(() -> {
                barrera.await(5, TimeUnit.SECONDS);
                dos.ejecutar(new MarcarAvisoLeidoCommand(aviso, cuenta));
                return null;
            });
            // get() propaga cualquier fallo de la escritura concurrente.
            escrituraUno.get(30, TimeUnit.SECONDS);
            escrituraDos.get(30, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }

        OffsetDateTime persistida = leidaEn(aviso);
        assertNotNull(persistida);
        assertTrue(persistida.toInstant().equals(primera.toInstant())
                        || persistida.toInstant().equals(segunda.toInstant()),
                "la fecha persistida procede de una de las dos escrituras");

        // Una escritura posterior, en otra transacción, no sustituye la primera lectura.
        servicioIndividual(primera.plusMinutes(30))
                .ejecutar(new MarcarAvisoLeidoCommand(aviso, cuenta));
        assertEquals(persistida.toInstant(), leidaEn(aviso).toInstant(),
                "la primera fecha se conserva ante reintentos");
        assertEquals(persistida.toInstant(),
                notificaciones.porId(aviso).orElseThrow().leidaEn().toInstant(),
                "el estado se puede volver a consultar desde otra transacción");

        Map<String, Object> fila = jdbc.queryForMap(
                "SELECT estado_entrega, intentos, fecha_entrega FROM notificaciones WHERE id = ?",
                aviso);
        assertEquals("ENTREGADA", fila.get("estado_entrega"));
        assertEquals(2, ((Number) fila.get("intentos")).intValue());
        assertNotNull(fila.get("fecha_entrega"));
    }

    @Test
    void avisoAjenoEInexistenteCompartenElRecursoNoEncontrado() {
        UUID cuenta = sembrarUsuario();
        UUID otra = sembrarUsuario();
        UUID ajeno = sembrarAviso(otra, "PENDIENTE", 0, null, null);
        UUID yaLeida = sembrarAviso(cuenta, "ENTREGADA", 1, ENTREGADA_EN, LEIDA_ANTES);

        MarcarAvisoLeidoUseCase servicio = servicioIndividual(LEIDA_ANTES.plusMinutes(1));
        BusinessException ajenoEx = assertThrows(BusinessException.class,
                () -> servicio.ejecutar(new MarcarAvisoLeidoCommand(ajeno, cuenta)));
        BusinessException inexistenteEx = assertThrows(BusinessException.class,
                () -> servicio.ejecutar(new MarcarAvisoLeidoCommand(UUID.randomUUID(), cuenta)));

        assertEquals("NOTIFICACION_NO_ENCONTRADA", ajenoEx.code());
        assertEquals(404, ajenoEx.status());
        assertEquals(ajenoEx.code(), inexistenteEx.code());
        assertEquals(ajenoEx.status(), inexistenteEx.status());
        assertEquals(ajenoEx.getMessage(), inexistenteEx.getMessage());
        assertNull(leidaEn(ajeno), "el aviso de otra cuenta permanece intacto");

        assertDoesNotThrow(() -> servicio.ejecutar(new MarcarAvisoLeidoCommand(yaLeida, cuenta)));
        assertEquals(LEIDA_ANTES.toInstant(), leidaEn(yaLeida).toInstant(),
                "un aviso propio ya leído se confirma sin cambiar la fecha");
    }

    @Test
    void laLecturaMasivaAlcanzaSoloPendientesDeLaCuentaYNoTocaPush() {
        UUID cuenta = sembrarUsuario();
        UUID otra = sembrarUsuario();
        UUID pendienteUno = sembrarAviso(cuenta, "ENTREGADA", 3, ENTREGADA_EN, null);
        UUID pendienteDos = sembrarAviso(cuenta, "PENDIENTE", 0, null, null);
        UUID yaLeida = sembrarAviso(cuenta, "ENTREGADA", 1, ENTREGADA_EN, LEIDA_ANTES);
        UUID ajena = sembrarAviso(otra, "PENDIENTE", 0, null, null);
        UUID trabajo = sembrarTrabajoPush(pendienteUno);
        Map<String, Object> trabajoAntes = filaTrabajo(trabajo);
        int trabajosAntes = contarTrabajos();

        servicioMasivo(LEIDA_ANTES.plusMinutes(1))
                .ejecutar(new MarcarTodasLeidasCommand(cuenta));

        assertNotNull(leidaEn(pendienteUno));
        assertNotNull(leidaEn(pendienteDos));
        assertEquals(LEIDA_ANTES.toInstant(), leidaEn(yaLeida).toInstant(),
                "una lectura anterior no se sustituye");
        assertNull(leidaEn(ajena), "los avisos de otra cuenta no se tocan");

        Map<String, Object> entrega = jdbc.queryForMap(
                "SELECT estado_entrega, intentos, fecha_entrega FROM notificaciones WHERE id = ?",
                pendienteUno);
        assertEquals("ENTREGADA", entrega.get("estado_entrega"));
        assertEquals(3, ((Number) entrega.get("intentos")).intValue());
        assertNotNull(entrega.get("fecha_entrega"));
        assertEquals(ENTREGADA_EN.toInstant(),
                ((java.sql.Timestamp) entrega.get("fecha_entrega")).toInstant());
        assertEquals(trabajoAntes, filaTrabajo(trabajo),
                "la lectura no modifica el trabajo push");
        assertEquals(trabajosAntes, contarTrabajos(), "no se crean ni eliminan trabajos push");

        // Un aviso publicado después de la actualización no queda leído por ella.
        UUID publicadaDespues = sembrarAviso(cuenta, "PENDIENTE", 0, null, null);
        assertNull(leidaEn(publicadaDespues));

        // Repetir y una cuenta sin pendientes terminan con éxito.
        assertDoesNotThrow(() -> servicioMasivo(LEIDA_ANTES.plusMinutes(2))
                .ejecutar(new MarcarTodasLeidasCommand(cuenta)));
        assertNotNull(leidaEn(publicadaDespues));
        assertDoesNotThrow(() -> servicioMasivo(LEIDA_ANTES.plusMinutes(3))
                .ejecutar(new MarcarTodasLeidasCommand(UUID.randomUUID())));
        assertEquals(1, trabajosAntes, "el escenario sembró un trabajo push");
    }

    private MarcarAvisoLeidoUseCase servicioIndividual(OffsetDateTime momento) {
        var service = new MarcarAvisoLeidoService(notificaciones,
                Clock.fixed(momento.toInstant(), ZoneOffset.UTC));
        var transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.executeWithoutResult(status -> service.ejecutar(command));
    }

    private MarcarTodasLeidasUseCase servicioMasivo(OffsetDateTime momento) {
        var service = new MarcarTodasLeidasService(notificaciones,
                Clock.fixed(momento.toInstant(), ZoneOffset.UTC));
        var transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.executeWithoutResult(status -> service.ejecutar(command));
    }

    private OffsetDateTime leidaEn(UUID aviso) {
        return jdbc.queryForObject("SELECT leida_en FROM notificaciones WHERE id = ?",
                (rs, fila) -> rs.getObject("leida_en", OffsetDateTime.class), aviso);
    }

    private Map<String, Object> filaTrabajo(UUID trabajo) {
        return jdbc.queryForMap("""
                SELECT estado, notificacion_id, encolado_en, expira_en, reclamacion_id,
                       finalizado_en, motivo_final
                FROM trabajos_push WHERE id = ?
                """, trabajo);
    }

    private int contarTrabajos() {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM trabajos_push", Integer.class);
        return total == null ? 0 : total;
    }

    private UUID sembrarUsuario() {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO usuarios (id, email, password_hash, rol, estado_cuenta, fecha_creacion)
                VALUES (?, ?, 'x', 'PRESTADOR', 'ACTIVA', ?)
                """, id, "prestador-" + id + "@test.local", OffsetDateTime.now());
        return id;
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

    private UUID sembrarTrabajoPush(UUID notificacion) {
        UUID trabajo = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO trabajos_push (id, notificacion_id, canal, estado, expira_en)
                VALUES (?, ?, 'PUSH', 'PENDIENTE_PREPARACION', ?)
                """, trabajo, notificacion, OffsetDateTime.now().plusHours(1));
        return trabajo;
    }

    private static String leer(String variable, String predeterminado) {
        String valor = System.getenv(variable);
        return valor == null || valor.isBlank() ? predeterminado : valor;
    }
}
