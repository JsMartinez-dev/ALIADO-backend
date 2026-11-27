package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.AvisoBandejaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.BandejaAvisosResult;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ConsultarBandejaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.ConsultarBandejaService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.ConsultarResumenNoLeidasService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarBandejaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarResumenNoLeidasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-BANDEJA-03 sobre PostgreSQL real: el cursor fecha–ID no duplica avisos con la misma
 * fecha, la última página no devuelve cursor, los filtros se combinan en la consulta, un
 * aviso más reciente aparece al refrescar la primera página y el resumen refleja la
 * primera lectura. La comparación lexicográfica del par y {@code SKIP}/{@code LIMIT} no se
 * pueden validar en H2.
 *
 * <p>Se ejecuta solo con {@code ALIADO_PG_URL} definido y una base desechable.</p>
 */
@EnabledIfEnvironmentVariable(named = "ALIADO_PG_URL", matches = ".+")
@Timeout(120)
@SpringBootTest
class ConsultarBandejaPostgresTest {

    private static final OffsetDateTime GENERADA =
            OffsetDateTime.of(2026, 3, 1, 9, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime LEIDA_EN =
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
    void laPaginacionConFechasIgualesNoDuplicaYLaUltimaPaginaNoTieneCursor() {
        UUID cuenta = sembrarUsuario();
        UUID otra = sembrarUsuario();
        List<UUID> esperadas = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            esperadas.add(sembrarAviso(cuenta, TipoNotificacion.NUEVA_OPORTUNIDAD, GENERADA, null));
        }
        sembrarAviso(otra, TipoNotificacion.NUEVA_OPORTUNIDAD, GENERADA, null);
        sembrarAviso(otra, TipoNotificacion.NUEVA_OPORTUNIDAD, GENERADA, null);

        ConsultarBandejaUseCase servicio = bandeja();
        List<UUID> vistas = new ArrayList<>();
        String cursor = null;
        int paginas = 0;
        do {
            BandejaAvisosResult pagina = servicio.ejecutar(
                    new ConsultarBandejaCommand(cuenta, null, null, 2, cursor));
            assertTrue(pagina.items().size() <= 2);
            pagina.items().stream().map(AvisoBandejaResult::id).forEach(vistas::add);
            cursor = pagina.cursorSiguiente();
            paginas++;
            assertTrue(paginas <= 5, "la paginación debe terminar");
        } while (cursor != null);

        assertEquals(3, paginas, "5 avisos con páginas de 2: 2 + 2 + 1");
        assertEquals(esperadas.size(), vistas.size());
        assertEquals(new HashSet<>(esperadas), new HashSet<>(vistas), "cada aviso aparece una vez");
        assertEquals(vistas.size(), new HashSet<>(vistas).size(), "no hay IDs repetidos");
        assertEquals(esperadas.size(), vistas.stream().filter(esperadas::contains).count());
    }

    @Test
    void losFiltrosDeEstadoYTipoSeCombinanYElResumenSigueLaPrimeraLectura() {
        UUID cuenta = sembrarUsuario();
        UUID otra = sembrarUsuario();
        UUID pendienteOportunidad = sembrarAviso(cuenta, TipoNotificacion.NUEVA_OPORTUNIDAD,
                GENERADA, null);
        UUID leidaOportunidad = sembrarAviso(cuenta, TipoNotificacion.NUEVA_OPORTUNIDAD,
                GENERADA.minusMinutes(1), LEIDA_EN);
        UUID pendientePropuesta = sembrarAviso(cuenta, TipoNotificacion.PROPUESTA_RECIBIDA,
                GENERADA.minusMinutes(2), null);
        UUID leidaPropuesta = sembrarAviso(cuenta, TipoNotificacion.PROPUESTA_RECIBIDA,
                GENERADA.minusMinutes(3), LEIDA_EN);
        sembrarAviso(otra, TipoNotificacion.NUEVA_OPORTUNIDAD, GENERADA, null);

        ConsultarResumenNoLeidasUseCase resumen = resumen();
        assertTrue(resumen.ejecutar(cuenta).hayNoLeidas());
        assertTrue(resumen.ejecutar(otra).hayNoLeidas(), "la otra cuenta conserva su pendiente");

        assertEquals(Set.of(pendienteOportunidad, pendientePropuesta),
                ids(bandeja().ejecutar(new ConsultarBandejaCommand(cuenta, null, null, 10, null))));
        assertEquals(Set.of(leidaOportunidad, leidaPropuesta),
                ids(bandeja().ejecutar(new ConsultarBandejaCommand(cuenta, "LEIDA", null, 10, null))));
        assertEquals(Set.of(pendienteOportunidad), ids(bandeja().ejecutar(
                new ConsultarBandejaCommand(cuenta, "NO_LEIDA", "NUEVA_OPORTUNIDAD", 10, null))));
        assertEquals(Set.of(pendientePropuesta, leidaPropuesta), ids(bandeja().ejecutar(
                new ConsultarBandejaCommand(cuenta, "TODAS", "PROPUESTA_RECIBIDA", 10, null))));

        assertEquals(2, notificaciones.marcarTodasLeidas(cuenta, LEIDA_EN));
        assertFalse(resumen.ejecutar(cuenta).hayNoLeidas());
        assertTrue(resumen.ejecutar(otra).hayNoLeidas(), "la lectura masiva no toca otra cuenta");

        UUID nueva = sembrarAviso(cuenta, TipoNotificacion.NUEVA_OPORTUNIDAD,
                GENERADA.plusMinutes(1), null);
        assertTrue(resumen.ejecutar(cuenta).hayNoLeidas(), "un aviso nuevo vuelve a encender la campana");
        assertEquals(1, notificaciones.marcarTodasLeidas(cuenta, LEIDA_EN.plusMinutes(1)));
        assertFalse(resumen.ejecutar(cuenta).hayNoLeidas(), "sin pendientes la campana se apaga");
        assertTrue(ids(bandeja().ejecutar(
                new ConsultarBandejaCommand(cuenta, "LEIDA", null, 10, null))).contains(nueva));
    }

    @Test
    void unAvisoMasRecienteApareceAlRefrescarLaPrimeraPagina() {
        UUID cuenta = sembrarUsuario();
        List<UUID> iniciales = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            iniciales.add(sembrarAviso(cuenta, TipoNotificacion.NUEVA_OPORTUNIDAD,
                    GENERADA.minusMinutes(i), null));
        }

        ConsultarBandejaUseCase servicio = bandeja();
        BandejaAvisosResult primera = servicio.ejecutar(
                new ConsultarBandejaCommand(cuenta, null, null, 2, null));
        assertEquals(2, primera.items().size());
        assertNotNull(primera.cursorSiguiente());

        UUID reciente = sembrarAviso(cuenta, TipoNotificacion.NUEVA_OPORTUNIDAD,
                GENERADA.plusMinutes(5), null);

        BandejaAvisosResult refrescada = servicio.ejecutar(
                new ConsultarBandejaCommand(cuenta, null, null, 2, null));
        assertEquals(reciente, refrescada.items().get(0).id(),
                "el aviso más reciente encabeza la primera página refrescada");

        List<UUID> vistas = new ArrayList<>();
        String cursor = null;
        do {
            BandejaAvisosResult pagina = servicio.ejecutar(
                    new ConsultarBandejaCommand(cuenta, null, null, 2, cursor));
            pagina.items().stream().map(AvisoBandejaResult::id).forEach(vistas::add);
            cursor = pagina.cursorSiguiente();
        } while (cursor != null);
        assertEquals(5, vistas.size());
        assertEquals(vistas.size(), new HashSet<>(vistas).size());
        assertTrue(vistas.containsAll(iniciales));
        assertTrue(vistas.contains(reciente));
    }

    @Test
    void elCursorNoPermiteVerAvisosDeOtraCuentaYSeRechazaConOtrosFiltros() {
        UUID cuenta = sembrarUsuario();
        UUID otra = sembrarUsuario();
        Set<UUID> deCuenta = new HashSet<>();
        Set<UUID> deOtra = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            deCuenta.add(sembrarAviso(cuenta, TipoNotificacion.NUEVA_OPORTUNIDAD,
                    GENERADA.minusMinutes(i), null));
        }
        for (int i = 0; i < 2; i++) {
            deOtra.add(sembrarAviso(otra, TipoNotificacion.NUEVA_OPORTUNIDAD,
                    GENERADA.minusMinutes(10 + i), null));
        }

        ConsultarBandejaUseCase servicio = bandeja();
        BandejaAvisosResult primeraCuenta = servicio.ejecutar(
                new ConsultarBandejaCommand(cuenta, null, null, 2, null));
        String cursorDeCuenta = primeraCuenta.cursorSiguiente();
        assertNotNull(cursorDeCuenta);

        BandejaAvisosResult reutilizadoEnOtra = servicio.ejecutar(
                new ConsultarBandejaCommand(otra, null, null, 10, cursorDeCuenta));
        assertFalse(reutilizadoEnOtra.items().isEmpty());
        assertTrue(reutilizadoEnOtra.items().stream()
                .allMatch(a -> deOtra.contains(a.id())), "solo devuelve avisos de la sesión");
        assertTrue(reutilizadoEnOtra.items().stream()
                .noneMatch(a -> deCuenta.contains(a.id())), "nunca expone avisos de otra cuenta");

        BusinessException error = assertThrows(BusinessException.class, () -> servicio.ejecutar(
                new ConsultarBandejaCommand(cuenta, "TODAS", null, 10, cursorDeCuenta)));
        assertEquals("CURSOR_INVALIDO", error.code());
        assertEquals(400, error.status());
    }

    private ConsultarBandejaUseCase bandeja() {
        var service = new ConsultarBandejaService(notificaciones, (propuesta, destinatario) -> java.util.Optional.empty());
        var transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    private ConsultarResumenNoLeidasUseCase resumen() {
        var service = new ConsultarResumenNoLeidasService(notificaciones);
        var transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        return destinatarioId -> transaction.execute(status -> service.ejecutar(destinatarioId));
    }

    private static Set<UUID> ids(BandejaAvisosResult resultado) {
        return resultado.items().stream().map(AvisoBandejaResult::id)
                .collect(java.util.stream.Collectors.toSet());
    }

    private UUID sembrarUsuario() {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO usuarios (id, email, password_hash, rol, estado_cuenta, fecha_creacion)
                VALUES (?, ?, 'x', 'PRESTADOR', 'ACTIVA', ?)
                """, id, "prestador-" + id + "@test.local", OffsetDateTime.now());
        return id;
    }

    private UUID sembrarAviso(UUID destinatario, TipoNotificacion tipo, OffsetDateTime generada,
                              OffsetDateTime leidaEn) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO notificaciones (id, destinatario_id, tipo, aggregate_id, event_version,
                                            detalle, canal, estado_entrega, intentos, fecha_generacion,
                                            fecha_entrega, leida_en)
                VALUES (?, ?, ?, ?, 1, 'detalle', 'PUSH', 'ENTREGADA', 1, ?, ?, ?)
                """, id, destinatario, tipo.name(), UUID.randomUUID(), generada,
                leidaEn == null ? generada.plusSeconds(1) : null, leidaEn);
        return id;
    }

    private static String leer(String variable, String predeterminado) {
        String valor = System.getenv(variable);
        return valor == null || valor.isBlank() ? predeterminado : valor;
    }
}
