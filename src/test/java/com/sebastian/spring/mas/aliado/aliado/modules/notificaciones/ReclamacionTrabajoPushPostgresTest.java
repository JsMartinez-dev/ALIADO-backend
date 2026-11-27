package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResultadoLotePush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.ProcesadorTrabajoPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.DispositivoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.ReclamacionTrabajoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.MetricasPushEnMemoria;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.ReclamacionTrabajoPushAdapter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
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
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-07 sobre PostgreSQL real: dos workers no comparten una reclamación vigente, la
 * reclamación vencida se recupera tanto en preparación como en envío sin una publicación
 * nueva, y los resultados tardíos o de una generación antigua afectan cero filas.
 *
 * <p>Se ejecuta solo con {@code ALIADO_PG_URL} definido y una base desechable; la suite
 * estándar (H2) queda intacta porque {@code SKIP LOCKED}, {@code NOTIFY} y la semántica
 * transaccional no se pueden validar en H2.</p>
 */
@EnabledIfEnvironmentVariable(named = "ALIADO_PG_URL", matches = ".+")
@Timeout(120)
@SpringBootTest
class ReclamacionTrabajoPushPostgresTest {

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
    @Autowired private ReclamacionTrabajoPushRepository reclamaciones;
    @Autowired private DispositivoPushRepository dispositivos;
    @Autowired private PlatformTransactionManager transactionManager;

    @BeforeEach
    void limpiarCola() {
        // Los usuarios sembrados no se borran: otras tablas del esquema los referencian y el
        // correo aleatorio evita colisiones. Las colas del piloto sí se aíslan por prueba.
        jdbc.update("DELETE FROM entregas_push");
        jdbc.update("DELETE FROM trabajos_push");
        jdbc.update("DELETE FROM vinculos_push");
        jdbc.update("DELETE FROM instalaciones_push");
        jdbc.update("DELETE FROM notificaciones");
    }

    @Test
    void dosWorkersNoCompartenUnaReclamacionVigente() throws Exception {
        UUID prestador = sembrarUsuario("PRESTADOR");
        sembrarTrabajoPendiente(prestador);
        sembrarTrabajoPendiente(prestador);
        sembrarTrabajoPendiente(prestador);

        ProcesadorRegistro primero = new ProcesadorRegistro();
        ProcesadorRegistro segundo = new ProcesadorRegistro();
        List<ReclamacionPush> reclamadas = new ArrayList<>();
        try (EscuchaSenalTrabajoPush escucha = EscuchaSenalTrabajoPush.abrir()) {
            CyclicBarrier barrera = new CyclicBarrier(2);
            ExecutorService pool = Executors.newFixedThreadPool(2);
            try {
                Future<ResultadoLotePush> uno = pool.submit(() -> {
                    barrera.await(5, TimeUnit.SECONDS);
                    return adaptador(8, Duration.ofSeconds(30), primero).procesarLoteElegible();
                });
                Future<ResultadoLotePush> dos = pool.submit(() -> {
                    barrera.await(5, TimeUnit.SECONDS);
                    return adaptador(8, Duration.ofSeconds(30), segundo).procesarLoteElegible();
                });
                assertEquals(3, uno.get(30, TimeUnit.SECONDS).procesados()
                        + dos.get(30, TimeUnit.SECONDS).procesados());
            } finally {
                pool.shutdownNow();
            }
            reclamadas.addAll(primero.reclamaciones);
            reclamadas.addAll(segundo.reclamaciones);

            assertEquals(3, reclamadas.size(), "cada trabajo se reclama una sola vez");
            assertEquals(3, reclamadas.stream().map(ReclamacionPush::trabajoId).distinct().count());
            assertEquals(3, reclamadas.stream().map(ReclamacionPush::reclamacionId).distinct().count(),
                    "ninguna reclamación vigente se comparte");
            assertTrue(escucha.esperar(5_000).length >= 1,
                    "la transacción de reclamación emite NOTIFY al confirmar");

            ResultadoLotePush sinElegibles = adaptador(8, Duration.ofSeconds(30),
                    new ProcesadorRegistro()).procesarLoteElegible();
            assertEquals(0, sinElegibles.procesados(),
                    "con reclamaciones vigentes no hay filas elegibles");
        }
    }

    @Test
    void laReclamacionVencidaSeRecuperaEnPreparacionSinNuevaPublicacion() throws Exception {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID trabajo = sembrarTrabajoPendiente(prestador);
        int notificacionesAntes = contar("notificaciones");

        ProcesadorQueFalla caido = new ProcesadorQueFalla();
        try (EscuchaSenalTrabajoPush escucha = EscuchaSenalTrabajoPush.abrir()) {
            assertThrows(IllegalStateException.class,
                    () -> adaptador(4, Duration.ofSeconds(1), caido).procesarLoteElegible(),
                    "simula la caída justo después del commit de la reclamación");
            ReclamacionPush primera = caido.unica();
            assertEquals(trabajo, primera.trabajoId());
            assertEquals(TipoReclamacionPush.PREPARACION, primera.tipo());
            assertTrue(escucha.esperar(5_000).length >= 1,
                    "la segunda instancia recibe la señal aunque la primera caiga");

            assertEquals(Optional.of(primera.venceEn()), reclamaciones.proximoPlazo(),
                    "el vencimiento persistido es el plazo que espera la otra instancia");

            Thread.sleep(1_200);

            ProcesadorRegistro segundo = new ProcesadorRegistro();
            ResultadoLotePush lote = adaptador(4, Duration.ofSeconds(30), segundo).procesarLoteElegible();
            assertEquals(1, lote.procesados(), "la reclamación vencida vuelve a ser elegible");
            ReclamacionPush vigente = segundo.unica();
            assertEquals(trabajo, vigente.trabajoId());
            assertNotEquals(primera.reclamacionId(), vigente.reclamacionId());

            assertFalse(enTransaccion(() ->
                            reclamaciones.confirmarPreparacion(trabajo, primera.reclamacionId())),
                    "el resultado de la reclamación vencida afecta cero filas");
            Map<String, Object> fila = jdbc.queryForMap(
                    "SELECT estado, reclamacion_id FROM trabajos_push WHERE id = ?", trabajo);
            assertEquals("PENDIENTE_PREPARACION", fila.get("estado"));
            assertEquals(vigente.reclamacionId(), fila.get("reclamacion_id"),
                    "la reclamación vigente no cambia por un resultado tardío");
            assertEquals(notificacionesAntes, contar("notificaciones"), "no hay publicación nueva");

            assertTrue(enTransaccion(() ->
                    reclamaciones.confirmarPreparacion(trabajo, vigente.reclamacionId())));
            assertEquals("PREPARADO", jdbc.queryForObject(
                    "SELECT estado FROM trabajos_push WHERE id = ?", String.class, trabajo));
        }
    }

    @Test
    void laReclamacionVencidaSeRecuperaEnEnvioYElResultadoTardioNoCambiaLaFila() throws Exception {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID instalacion = sembrarDispositivo(prestador);
        UUID trabajo = sembrarTrabajoPreparado(prestador);
        UUID entrega = sembrarEntrega(trabajo, instalacion, 1, Instant.now().minusSeconds(1));

        ProcesadorQueFalla caido = new ProcesadorQueFalla();
        assertThrows(IllegalStateException.class,
                () -> adaptador(4, Duration.ofSeconds(1), caido).procesarLoteElegible());
        ReclamacionPush primera = caido.unica();
        assertEquals(TipoReclamacionPush.ENVIO, primera.tipo());
        assertEquals(entrega, primera.entregaId());
        assertEquals(instalacion, primera.instalacionId());
        assertEquals(1L, primera.generacion());

        Thread.sleep(1_200);

        ProcesadorRegistro segundo = new ProcesadorRegistro();
        assertEquals(1, adaptador(4, Duration.ofSeconds(30), segundo).procesarLoteElegible().procesados());
        ReclamacionPush vigente = segundo.unica();
        assertNotEquals(primera.reclamacionId(), vigente.reclamacionId());

        ResultadoEntregaPush aceptacionTardia = new ResultadoEntregaPush(entrega,
                primera.reclamacionId(), 1, EstadoEntregaPush.ACEPTADA, 1, null, "fcm-viejo", null);
        assertFalse(enTransaccion(() -> reclamaciones.guardarResultadoEntrega(aceptacionTardia)),
                "el resultado de la reclamación vencida afecta cero filas");
        Map<String, Object> pendiente = filaEntrega(entrega);
        assertEquals("PENDIENTE", pendiente.get("estado"));
        assertEquals(vigente.reclamacionId(), pendiente.get("reclamacion_id"));
        assertEquals(null, pendiente.get("id_mensaje_fcm"));

        ResultadoEntregaPush aceptacion = new ResultadoEntregaPush(entrega, vigente.reclamacionId(),
                1, EstadoEntregaPush.ACEPTADA, 1, null, "fcm-nuevo", null);
        assertTrue(enTransaccion(() -> reclamaciones.guardarResultadoEntrega(aceptacion)));
        Map<String, Object> aceptada = filaEntrega(entrega);
        assertEquals("ACEPTADA", aceptada.get("estado"));
        assertEquals("fcm-nuevo", aceptada.get("id_mensaje_fcm"));
        assertEquals(null, aceptada.get("reclamacion_id"));
        assertEquals(null, aceptada.get("proximo_intento_en"));
        assertTrue(aceptada.get("finalizada_en") != null, "un estado terminal fija su cierre");

        ResultadoEntregaPush reintento = new ResultadoEntregaPush(entrega, vigente.reclamacionId(),
                1, EstadoEntregaPush.PENDIENTE, 2, Instant.now().plusSeconds(30), null, "temporal");
        assertFalse(enTransaccion(() -> reclamaciones.guardarResultadoEntrega(reintento)),
                "un resultado posterior no sobrescribe el estado actual");
        assertEquals("ACEPTADA", filaEntrega(entrega).get("estado"));
    }

    @Test
    void elResultadoDeUnaGeneracionObsoletaNoInvalidaElTokenRenovado() {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID instalacion = sembrarDispositivo(prestador);
        UUID trabajo = sembrarTrabajoPreparado(prestador);
        UUID entrega = sembrarEntrega(trabajo, instalacion, 1, Instant.now().minusSeconds(1));

        ProcesadorRegistro procesador = new ProcesadorRegistro();
        adaptador(4, Duration.ofSeconds(30), procesador).procesarLoteElegible();
        ReclamacionPush reclamacion = procesador.unica();

        rotarDispositivo(instalacion, prestador);

        ResultadoEntregaPush generacionEquivocada = new ResultadoEntregaPush(entrega,
                reclamacion.reclamacionId(), 2, EstadoEntregaPush.ACEPTADA, 1, null, "fcm-gen2", null);
        assertFalse(enTransaccion(() -> reclamaciones.guardarResultadoEntrega(generacionEquivocada)),
                "un resultado con otra generación no se aplica a la entrega");

        ResultadoEntregaPush resultadoGen1 = new ResultadoEntregaPush(entrega,
                reclamacion.reclamacionId(), 1, EstadoEntregaPush.ACEPTADA, 1, null, "fcm-gen1", null);
        assertTrue(enTransaccion(() -> reclamaciones.guardarResultadoEntrega(resultadoGen1)));

        assertFalse(enTransaccion(() -> dispositivos.revocarVinculo(instalacion, 1,
                        OffsetDateTime.now())),
                "la revocación de la generación antigua no encuentra un vínculo activo");
        assertEquals(1, jdbc.queryForObject("""
                SELECT COUNT(*) FROM vinculos_push
                WHERE instalacion_id = ? AND generacion = 2 AND revocado_en IS NULL
                """, Integer.class, instalacion),
                "la generación renovada sigue activa");
    }

    @Test
    void elLoteRespetaElTopeTotalYElCupoLibrePasaAlOtroTipo() {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID instalacion = sembrarDispositivo(prestador);
        sembrarTrabajoPendiente(prestador);
        for (int i = 0; i < 6; i++) {
            UUID trabajo = sembrarTrabajoPreparado(prestador);
            sembrarEntrega(trabajo, instalacion, 1, Instant.now().minusSeconds(1));
        }

        ProcesadorRegistro procesador = new ProcesadorRegistro();
        ResultadoLotePush lote = adaptador(6, Duration.ofSeconds(45), procesador).procesarLoteElegible();

        assertEquals(6, lote.procesados(), "el tope total del lote se respeta");
        long preparaciones = procesador.reclamaciones.stream()
                .filter(reclamacion -> reclamacion.tipo() == TipoReclamacionPush.PREPARACION)
                .count();
        assertEquals(1, preparaciones);
        assertEquals(5, procesador.reclamaciones.size() - preparaciones,
                "el cupo libre de preparación lo usan las entregas");
        assertTrue(lote.quedanElegibles(), "la cola de entregas agotó su cupo");
    }

    @Test
    void elProximoPlazoIncluyeElReintentoDeUnaEntrega() {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID instalacion = sembrarDispositivo(prestador);
        UUID trabajoPreparado = sembrarTrabajoPreparado(prestador);
        Instant proximoIntento = Instant.now().plusSeconds(3);
        sembrarEntrega(trabajoPreparado, instalacion, 1, proximoIntento);

        UUID pendiente = sembrarTrabajoPendiente(prestador);
        jdbc.update("UPDATE trabajos_push SET expira_en = ? WHERE id = ?",
                instante(Instant.now().plusSeconds(40)), pendiente);

        Instant plazo = reclamaciones.proximoPlazo().orElseThrow();
        assertTrue(Math.abs(Duration.between(proximoIntento, plazo).toMillis()) < 1_500,
                "el reintento de 3 s es el menor plazo");
    }

    @Test
    void elProximoPlazoIncluyeLaExpiracionDeUnTrabajoPendiente() {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID trabajo = sembrarTrabajoPendiente(prestador);
        Instant expiracion = Instant.now().plusSeconds(2);
        jdbc.update("UPDATE trabajos_push SET expira_en = ? WHERE id = ?",
                instante(expiracion), trabajo);

        Instant plazo = reclamaciones.proximoPlazo().orElseThrow();
        assertTrue(Math.abs(Duration.between(expiracion, plazo).toMillis()) < 1_500,
                "la expiración despierta al worker para cerrar el trabajo");
    }

    private ReclamacionTrabajoPushAdapter adaptador(int maximoPorTipo, Duration vigencia,
                                                    ProcesadorTrabajoPushPort procesador) {
        TransactionTemplate transaccion = new TransactionTemplate(transactionManager);
        transaccion.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return new ReclamacionTrabajoPushAdapter(reclamaciones, procesador,
                new MetricasPushEnMemoria(Clock.systemUTC()), transaccion,
                maximoPorTipo, vigencia);
    }

    private boolean enTransaccion(BooleanSupplier operacion) {
        Boolean resultado = new TransactionTemplate(transactionManager)
                .execute(estado -> operacion.getAsBoolean());
        return Boolean.TRUE.equals(resultado);
    }

    private UUID sembrarUsuario(String rol) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO usuarios (id, email, password_hash, rol, estado_cuenta, fecha_creacion)
                VALUES (?, ?, 'x', ?, 'ACTIVA', ?)
                """, id, rol.toLowerCase() + "-" + id + "@test.local", rol, OffsetDateTime.now());
        return id;
    }

    private UUID sembrarTrabajoPendiente(UUID destinatario) {
        return sembrarTrabajoPendiente(destinatario, Instant.now().plusSeconds(3_600));
    }

    private UUID sembrarTrabajoPendiente(UUID destinatario, Instant expiraEn) {
        UUID notificacion = sembrarNotificacion(destinatario);
        UUID trabajo = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO trabajos_push (id, notificacion_id, canal, estado, expira_en)
                VALUES (?, ?, 'PUSH', 'PENDIENTE_PREPARACION', ?)
                """, trabajo, notificacion, instante(expiraEn));
        return trabajo;
    }

    private UUID sembrarTrabajoPreparado(UUID destinatario) {
        UUID notificacion = sembrarNotificacion(destinatario);
        UUID trabajo = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO trabajos_push (id, notificacion_id, canal, estado, expira_en)
                VALUES (?, ?, 'PUSH', 'PREPARADO', ?)
                """, trabajo, notificacion, instante(Instant.now().plusSeconds(3_600)));
        return trabajo;
    }

    private UUID sembrarNotificacion(UUID destinatario) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO notificaciones (id, destinatario_id, tipo, aggregate_id, event_version,
                                            detalle, canal, estado_entrega, intentos, fecha_generacion)
                VALUES (?, ?, 'NUEVA_OPORTUNIDAD', ?, 1, 'detalle', 'PUSH', 'PENDIENTE', 0, ?)
                """, id, destinatario, UUID.randomUUID(), OffsetDateTime.now());
        return id;
    }

    private UUID sembrarDispositivo(UUID usuario) {
        UUID instalacion = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO instalaciones_push (id, plataforma, prueba_sha256)
                VALUES (?, 'ANDROID', ?)
                """, instalacion, bytesLlenos(7));
        insertarVinculo(instalacion, 1, usuario);
        return instalacion;
    }

    private void insertarVinculo(UUID instalacion, long generacion, UUID usuario) {
        String token = "token-" + UUID.randomUUID();
        jdbc.update("""
                INSERT INTO vinculos_push (instalacion_id, generacion, usuario_id, token_fcm,
                                           token_fcm_sha256, revocacion_sha256, vinculado_en,
                                           revocado_en)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, NULL)
                """, instalacion, generacion, usuario, token, sha256(token), bytesAleatorios());
    }

    private void rotarDispositivo(UUID instalacion, UUID usuario) {
        jdbc.update("""
                UPDATE vinculos_push
                SET revocado_en = CURRENT_TIMESTAMP, token_fcm = NULL, token_fcm_sha256 = NULL
                WHERE instalacion_id = ? AND generacion = 1
                """, instalacion);
        insertarVinculo(instalacion, 2, usuario);
    }

    private UUID sembrarEntrega(UUID trabajo, UUID instalacion, long generacion,
                                Instant proximoIntento) {
        UUID entrega = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO entregas_push (id, trabajo_id, instalacion_id, generacion, estado,
                                           intentos, proximo_intento_en)
                VALUES (?, ?, ?, ?, 'PENDIENTE', 0, ?)
                """, entrega, trabajo, instalacion, generacion, instante(proximoIntento));
        return entrega;
    }

    private Map<String, Object> filaEntrega(UUID entrega) {
        return jdbc.queryForMap("""
                SELECT estado, reclamacion_id, id_mensaje_fcm, proximo_intento_en, finalizada_en
                FROM entregas_push WHERE id = ?
                """, entrega);
    }

    private int contar(String tabla) {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM " + tabla, Integer.class);
        return total == null ? 0 : total;
    }

    private static OffsetDateTime instante(Instant momento) {
        return OffsetDateTime.ofInstant(momento, ZoneOffset.UTC);
    }

    private static byte[] bytesLlenos(int valor) {
        byte[] bytes = new byte[32];
        Arrays.fill(bytes, (byte) valor);
        return bytes;
    }

    private static byte[] bytesAleatorios() {
        byte[] bytes = new byte[32];
        new Random().nextBytes(bytes);
        return bytes;
    }

    private static byte[] sha256(String texto) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(texto.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String leer(String variable, String porDefecto) {
        String valor = System.getenv(variable);
        return valor == null || valor.isBlank() ? porDefecto : valor;
    }

    static final class ProcesadorRegistro implements ProcesadorTrabajoPushPort {
        final List<ReclamacionPush> reclamaciones = new CopyOnWriteArrayList<>();

        @Override
        public void procesar(ReclamacionPush reclamacion) {
            reclamaciones.add(reclamacion);
        }

        ReclamacionPush unica() {
            assertEquals(1, reclamaciones.size(), "se esperaba una sola reclamación");
            return reclamaciones.get(0);
        }
    }

    static final class ProcesadorQueFalla implements ProcesadorTrabajoPushPort {
        final List<ReclamacionPush> reclamaciones = new CopyOnWriteArrayList<>();

        @Override
        public void procesar(ReclamacionPush reclamacion) {
            reclamaciones.add(reclamacion);
            throw new IllegalStateException("Caída simulada después del commit de la reclamación");
        }

        ReclamacionPush unica() {
            assertEquals(1, reclamaciones.size(), "se esperaba una sola reclamación");
            return reclamaciones.get(0);
        }
    }
}
