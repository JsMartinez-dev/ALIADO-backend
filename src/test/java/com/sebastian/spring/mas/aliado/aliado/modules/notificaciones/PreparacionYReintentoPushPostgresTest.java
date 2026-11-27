package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.EnvioPushPendiente;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RegistrarResultadoEntregaPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.EntregaPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.ProcesadorTrabajoPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.RevalidacionOportunidadPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.IniciarEntregaPushService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.PrepararEntregasPushService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.RegistrarResultadoEntregaPushService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.CodigosEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ConfiguracionCanalPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.MensajePush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.PendientesPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.PoliticaReintentoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEnvioPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.VentanaEnvioPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.DispositivoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.ReclamacionTrabajoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.MetricasPushEnMemoria;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.ProcesadorTrabajoPushAdapter;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.ReclamacionTrabajoPushAdapter;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ConsultaPreferenciasPort;
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
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-08 sobre PostgreSQL real: preparación idempotente y atómica, A aceptado y B
 * temporal con Retry-After, supresión antes del reintento (vínculo revocado, generación
 * rotada, preferencia apagada, oportunidad cerrada y vigencia vencida), guardia de la
 * ventana de envío con liberación de la reclamación, cierre del padre y concurrencia de
 * resultados.
 *
 * <p>Se ejecuta solo con {@code ALIADO_PG_URL} definido y una base desechable. El
 * adaptador de entrega es un doble que nunca contacta con FCM.</p>
 */
@EnabledIfEnvironmentVariable(named = "ALIADO_PG_URL", matches = ".+")
@Timeout(180)
@SpringBootTest
class PreparacionYReintentoPushPostgresTest {

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
    @Autowired private NotificacionRepository notificaciones;
    @Autowired private PlatformTransactionManager transactionManager;

    @BeforeEach
    void limpiarCola() {
        jdbc.update("DELETE FROM entregas_push");
        jdbc.update("DELETE FROM trabajos_push");
        jdbc.update("DELETE FROM vinculos_push");
        jdbc.update("DELETE FROM instalaciones_push");
        jdbc.update("DELETE FROM notificaciones");
    }

    @Test
    void aceptaAYReintentaSoloBConRetryAfterYPersisteElPlazoConSenal() throws Exception {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID notificacion = sembrarNotificacion(prestador);
        UUID trabajo = sembrarTrabajoPendiente(notificacion, Instant.now().plusSeconds(3_600));
        UUID instalacionA = sembrarDispositivo(prestador, "token-a");
        UUID instalacionB = sembrarDispositivo(prestador, "token-b");

        EntregaPushFalsa entrega = new EntregaPushFalsa();
        entrega.responder("token-a", ResultadoEnvioPush.aceptado("fcm-a"));
        entrega.responder("token-b", ResultadoEnvioPush.temporal("UNAVAILABLE",
                Instant.now().plusSeconds(4)));
        ProcesadorTrabajoPushAdapter procesador = procesador(entrega, true, true,
                Duration.ofSeconds(1));
        ReclamacionTrabajoPushAdapter reclamacion = adaptador(10, Duration.ofSeconds(30), procesador);

        try (EscuchaSenalTrabajoPush escucha = EscuchaSenalTrabajoPush.abrir()) {
            assertEquals(1, reclamacion.procesarLoteElegible().procesados(),
                    "la primera iteración prepara el padre");
            assertEquals(2, reclamacion.procesarLoteElegible().procesados(),
                    "la segunda iteración envía las dos entregas");

            Map<String, Object> filaA = filaEntrega(porInstalacion(trabajo, instalacionA));
            Map<String, Object> filaB = filaEntrega(porInstalacion(trabajo, instalacionB));
            assertEquals("ACEPTADA", filaA.get("estado"));
            assertEquals("fcm-a", filaA.get("id_mensaje_fcm"));
            assertEquals(1, filaA.get("intentos"));
            assertNull(filaA.get("proximo_intento_en"));

            assertEquals("PENDIENTE", filaB.get("estado"));
            assertEquals(1, filaB.get("intentos"));
            assertEquals("UNAVAILABLE", filaB.get("ultimo_codigo_error"));
            assertNull(filaB.get("reclamacion_id"));
            Instant proximoB = ((java.sql.Timestamp) filaB.get("proximo_intento_en")).toInstant();
            assertTrue(proximoB.isAfter(Instant.now().plusSeconds(2)),
                    "Retry-After prevalece sobre el backoff de 1 s");
            assertEquals(proximoB, reclamaciones.proximoPlazo().orElseThrow(),
                    "el temporizador usa exactamente el plazo persistido de B");

            assertEquals("PREPARADO", estadoTrabajo(trabajo),
                    "el padre sigue activo mientras B no es terminal");
            assertEquals(1, contar("notificaciones"), "no hay publicación nueva");
            assertTrue(escucha.esperar(5_000).length >= 1,
                    "la reprogramación y los resultados emiten NOTIFY");

            // Simula el vencimiento del temporizador y la revocación de B antes del reintento.
            jdbc.update("UPDATE entregas_push SET proximo_intento_en = CURRENT_TIMESTAMP - interval '1 second' WHERE trabajo_id = ? AND instalacion_id = ?",
                    trabajo, instalacionB);
            assertTrue(dispositivos.revocarVinculo(instalacionB, 1, OffsetDateTime.now()));

            assertEquals(1, reclamacion.procesarLoteElegible().procesados(),
                    "solo B vuelve a ser elegible");
            assertEquals(1, entrega.enviosA("token-b"),
                    "B no se vuelve a enviar: se suprime por el vínculo revocado");
            Map<String, Object> suprimida = filaEntrega(porInstalacion(trabajo, instalacionB));
            assertEquals("SUPRIMIDA", suprimida.get("estado"));
            assertEquals(CodigosEntregaPush.VINCULO_REVOCADO, suprimida.get("ultimo_codigo_error"));
            assertEquals("FINALIZADO", estadoTrabajo(trabajo),
                    "el padre termina cuando ya no quedan entregas no terminales");
            assertEquals("ENTREGAS_TERMINALES", motivoTrabajo(trabajo));
            assertEquals("PENDIENTE", jdbc.queryForObject(
                    "SELECT estado_entrega FROM notificaciones WHERE id = ?", String.class, notificacion),
                    "la bandeja conserva el aviso");
            assertEquals(0, reclamacion.procesarLoteElegible().procesados(),
                    "las entregas terminales no vuelven a ser elegibles");
        }
    }

    @Test
    void laPreparacionReintentadaNoDuplicaEntregasYConfirmaElPadre() {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID notificacion = sembrarNotificacion(prestador);
        UUID trabajo = sembrarTrabajoPendiente(notificacion, Instant.now().plusSeconds(3_600));
        UUID instalacionA = sembrarDispositivo(prestador, "token-a");
        UUID instalacionB = sembrarDispositivo(prestador, "token-b");
        sembrarEntrega(trabajo, instalacionA, 1, Instant.now().plusSeconds(3_600));
        sembrarEntrega(trabajo, instalacionB, 1, Instant.now().plusSeconds(3_600));

        ProcesadorTrabajoPushAdapter procesador = procesador(new EntregaPushFalsa(), true, true,
                Duration.ofSeconds(1));
        assertEquals(1, adaptador(10, Duration.ofSeconds(30), procesador)
                .procesarLoteElegible().procesados());

        assertEquals(2, jdbc.queryForObject(
                "SELECT COUNT(*) FROM entregas_push WHERE trabajo_id = ?", Integer.class, trabajo),
                "ON CONFLICT conserva las entregas ya creadas");
        assertEquals("PREPARADO", estadoTrabajo(trabajo));
    }

    @Test
    void laPreparacionSinConfirmarNoCreaEntregasYLaFilaVuelveASerReclamable() {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID notificacion = sembrarNotificacion(prestador);
        UUID trabajo = sembrarTrabajoPendiente(notificacion, Instant.now().plusSeconds(3_600));
        UUID instalacion = sembrarDispositivo(prestador, "token-a");
        UUID reclamacionVencida = UUID.randomUUID();
        jdbc.update("""
                UPDATE trabajos_push
                SET reclamacion_id = ?,
                    reclamacion_vence_en = CURRENT_TIMESTAMP - interval '1 second'
                WHERE id = ?
                """, reclamacionVencida, trabajo);

        EntregaPushFalsa entrega = new EntregaPushFalsa();
        entrega.responder("token-a", ResultadoEnvioPush.aceptado("fcm-a"));
        ProcesadorTrabajoPushAdapter procesador = procesador(entrega, true, true,
                Duration.ofSeconds(1));

        procesador.procesar(ReclamacionPush.preparacion(trabajo, reclamacionVencida,
                Instant.now().minusSeconds(1)));

        assertEquals(0, contar("entregas_push"),
                "una preparación que no confirma no deja entregas creadas");
        assertEquals("PENDIENTE_PREPARACION", estadoTrabajo(trabajo));

        // La reclamación venció: el padre vuelve a ser reclamable y se prepara y envía.
        ReclamacionTrabajoPushAdapter reclamacion = adaptador(10, Duration.ofSeconds(30), procesador);
        assertEquals(1, reclamacion.procesarLoteElegible().procesados());
        assertEquals(1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM entregas_push WHERE trabajo_id = ?", Integer.class, trabajo),
                "la repetición de la preparación crea una sola entrega por dispositivo");
        assertEquals(1, reclamacion.procesarLoteElegible().procesados());
        assertEquals(1, entrega.totalEnvios());
        assertEquals("ACEPTADA", filaEntrega(porInstalacion(trabajo, instalacion)).get("estado"));
        assertEquals("FINALIZADO", estadoTrabajo(trabajo));
    }

    @Test
    void sinTiempoSuficienteNoSeEnviaYLaEntregaSeReclamaYEnviaUnaSolaVezDespues() {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID notificacion = sembrarNotificacion(prestador);
        UUID trabajo = sembrarTrabajoPreparado(notificacion, Instant.now().plusSeconds(3_600));
        UUID instalacion = sembrarDispositivo(prestador, "token-a");
        UUID entregaId = sembrarEntrega(trabajo, instalacion, 1, Instant.now().minusSeconds(1));
        UUID reclamacionId = UUID.randomUUID();
        jdbc.update("""
                UPDATE entregas_push
                SET reclamacion_id = ?,
                    reclamacion_vence_en = CURRENT_TIMESTAMP + interval '3 seconds'
                WHERE id = ?
                """, reclamacionId, entregaId);

        EntregaPushFalsa entrega = new EntregaPushFalsa();
        entrega.responder("token-a", ResultadoEnvioPush.aceptado("fcm-a"));
        ProcesadorTrabajoPushAdapter procesador = procesador(entrega, true, true,
                Duration.ofSeconds(1), Clock.systemUTC(),
                new VentanaEnvioPush(Duration.ofSeconds(5), Duration.ofSeconds(1)));

        procesador.procesar(ReclamacionPush.envio(entregaId, trabajo, instalacion, 1,
                reclamacionId, Instant.now().plusSeconds(3)));

        assertEquals(0, entrega.totalEnvios(),
                "sin ventana para el timeout y el margen no se llama al proveedor");
        Map<String, Object> liberada = filaEntrega(entregaId);
        assertEquals("PENDIENTE", liberada.get("estado"));
        assertNull(liberada.get("reclamacion_id"), "la reclamación se libera");
        assertNull(liberada.get("reclamacion_vence_en"));
        assertEquals(0, liberada.get("intentos"), "la liberación no consume un intento");

        // La fila liberada es reclamable de inmediato: el ciclo siguiente la envía una vez.
        ReclamacionTrabajoPushAdapter reclamacion = adaptador(10, Duration.ofSeconds(30), procesador);
        assertEquals(1, reclamacion.procesarLoteElegible().procesados());
        assertEquals(1, entrega.enviosA("token-a"), "se envía una sola vez tras la liberación");
        assertEquals("ACEPTADA", filaEntrega(entregaId).get("estado"));
        assertEquals("FINALIZADO", estadoTrabajo(trabajo));
    }

    @Test
    void elPlazoDeUnaEntregaReclamadaEsSuVencimientoYNoElIntentoPasado() {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID notificacion = sembrarNotificacion(prestador);
        UUID trabajo = sembrarTrabajoPreparado(notificacion, Instant.now().plusSeconds(3_600));
        UUID instalacion = sembrarDispositivo(prestador, "token-a");
        sembrarEntrega(trabajo, instalacion, 1, Instant.now().minusSeconds(1));

        ReclamacionTrabajoPushAdapter reclamacion = adaptador(10, Duration.ofSeconds(30),
                new RegistroProcesador());
        assertEquals(1, reclamacion.procesarLoteElegible().procesados(),
                "el registro deja la entrega reclamada sin enviar");

        Instant plazo = reclamacion.proximoPlazo().orElseThrow();
        assertTrue(plazo.isAfter(Instant.now().plusSeconds(20)),
                "el temporizador espera el vencimiento de la reclamación, no un plazo pasado");
    }

    @Test
    void unaEntregaPendienteDeUnPadreFinalizadoNoProgramaPlazo() {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID notificacion = sembrarNotificacion(prestador);
        UUID trabajo = sembrarTrabajoPreparado(notificacion, Instant.now().plusSeconds(3_600));
        UUID instalacion = sembrarDispositivo(prestador, "token-a");
        sembrarEntrega(trabajo, instalacion, 1, Instant.now().minusSeconds(1));
        jdbc.update("""
                UPDATE trabajos_push
                SET estado = 'FINALIZADO',
                    finalizado_en = CURRENT_TIMESTAMP,
                    motivo_final = 'ENTREGAS_TERMINALES'
                WHERE id = ?
                """, trabajo);

        assertTrue(adaptador(10, Duration.ofSeconds(30), new RegistroProcesador())
                        .proximoPlazo().isEmpty(),
                "una entrega huérfana no debe programar un temporizador de retraso cero");
    }

    @Test
    void preferenciaApagadaAntesDelReintentoSuprimeLaEntregaSinLlamarAlProveedor() {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID notificacion = sembrarNotificacion(prestador);
        UUID trabajo = sembrarTrabajoPreparado(notificacion, Instant.now().plusSeconds(3_600));
        UUID instalacion = sembrarDispositivo(prestador, "token-a");
        sembrarEntrega(trabajo, instalacion, 1, Instant.now().minusSeconds(1));

        EntregaPushFalsa entrega = new EntregaPushFalsa();
        adaptador(10, Duration.ofSeconds(30), procesador(entrega, false, true,
                Duration.ofSeconds(1))).procesarLoteElegible();

        assertEquals(0, entrega.totalEnvios(), "no se contacta al proveedor");
        assertEquals("SUPRIMIDA", filaEntrega(porInstalacion(trabajo, instalacion)).get("estado"));
        assertEquals(CodigosEntregaPush.PREFERENCIA_DESHABILITADA,
                filaEntrega(porInstalacion(trabajo, instalacion)).get("ultimo_codigo_error"));
        assertEquals("FINALIZADO", estadoTrabajo(trabajo));
    }

    @Test
    void oportunidadCerradaAntesDelReintentoSuprimeLaEntrega() {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID notificacion = sembrarNotificacion(prestador);
        UUID trabajo = sembrarTrabajoPreparado(notificacion, Instant.now().plusSeconds(3_600));
        UUID instalacion = sembrarDispositivo(prestador, "token-a");
        sembrarEntrega(trabajo, instalacion, 1, Instant.now().minusSeconds(1));

        EntregaPushFalsa entrega = new EntregaPushFalsa();
        adaptador(10, Duration.ofSeconds(30), procesador(entrega, true, false,
                Duration.ofSeconds(1))).procesarLoteElegible();

        assertEquals(0, entrega.totalEnvios());
        assertEquals(CodigosEntregaPush.OPORTUNIDAD_NO_VIGENTE,
                filaEntrega(porInstalacion(trabajo, instalacion)).get("ultimo_codigo_error"));
        assertEquals("FINALIZADO", estadoTrabajo(trabajo));
    }

    @Test
    void tokenRotadoAntesDelReintentoSuprimeLaGeneracionAnterior() {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID notificacion = sembrarNotificacion(prestador);
        UUID trabajo = sembrarTrabajoPreparado(notificacion, Instant.now().plusSeconds(3_600));
        UUID instalacion = sembrarDispositivo(prestador, "token-a");
        sembrarEntrega(trabajo, instalacion, 1, Instant.now().minusSeconds(1));
        rotarDispositivo(instalacion, prestador, 2);

        EntregaPushFalsa entrega = new EntregaPushFalsa();
        adaptador(10, Duration.ofSeconds(30), procesador(entrega, true, true,
                Duration.ofSeconds(1))).procesarLoteElegible();

        assertEquals(0, entrega.totalEnvios());
        assertEquals(CodigosEntregaPush.GENERACION_OBSOLETA,
                filaEntrega(porInstalacion(trabajo, instalacion)).get("ultimo_codigo_error"));
        assertEquals(1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM vinculos_push WHERE instalacion_id = ? AND generacion = 2 AND revocado_en IS NULL",
                Integer.class, instalacion), "la generación nueva sigue activa");
    }

    @Test
    void laVigenciaVencidaCierraLaEntregaPendienteYElPadre() {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID notificacion = sembrarNotificacion(prestador);
        UUID trabajo = sembrarTrabajoPreparado(notificacion, Instant.now().minusSeconds(1));
        UUID instalacion = sembrarDispositivo(prestador, "token-a");
        sembrarEntrega(trabajo, instalacion, 1, Instant.now().plusSeconds(3_600));

        EntregaPushFalsa entrega = new EntregaPushFalsa();
        assertEquals(1, adaptador(10, Duration.ofSeconds(30), procesador(entrega, true, true,
                        Duration.ofSeconds(1))).procesarLoteElegible().procesados(),
                "la expiración del padre hace elegible una entrega con plazo futuro");

        assertEquals(0, entrega.totalEnvios());
        assertEquals("EXPIRADA", filaEntrega(porInstalacion(trabajo, instalacion)).get("estado"));
        assertEquals(CodigosEntregaPush.VIGENCIA_AGOTADA,
                filaEntrega(porInstalacion(trabajo, instalacion)).get("ultimo_codigo_error"));
        assertEquals("FINALIZADO", estadoTrabajo(trabajo));
    }

    @Test
    void losIntentosAgotadosTerminanLaEntregaYElPadre() {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID notificacion = sembrarNotificacion(prestador);
        UUID trabajo = sembrarTrabajoPreparado(notificacion, Instant.now().plusSeconds(3_600));
        UUID instalacion = sembrarDispositivo(prestador, "token-a");
        sembrarEntregaConIntentos(trabajo, instalacion, 1, Instant.now().minusSeconds(1), 4);

        EntregaPushFalsa entrega = new EntregaPushFalsa();
        entrega.responder("token-a", ResultadoEnvioPush.temporal("UNAVAILABLE", null));
        adaptador(10, Duration.ofSeconds(30), procesador(entrega, true, true,
                Duration.ofSeconds(1))).procesarLoteElegible();

        Map<String, Object> fila = filaEntrega(porInstalacion(trabajo, instalacion));
        assertEquals(1, entrega.totalEnvios());
        assertEquals("FALLIDA_DEFINITIVA", fila.get("estado"));
        assertEquals(5, fila.get("intentos"));
        assertEquals(CodigosEntregaPush.INTENTOS_AGOTADOS, fila.get("ultimo_codigo_error"));
        assertNull(fila.get("proximo_intento_en"));
        assertEquals("FINALIZADO", estadoTrabajo(trabajo));
    }

    @Test
    void dosResultadosConcurrentesCierranElPadreUnaSolaVez() throws Exception {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID notificacion = sembrarNotificacion(prestador);
        UUID trabajo = sembrarTrabajoPreparado(notificacion, Instant.now().plusSeconds(3_600));
        UUID instalacionA = sembrarDispositivo(prestador, "token-a");
        UUID instalacionB = sembrarDispositivo(prestador, "token-b");
        sembrarEntrega(trabajo, instalacionA, 1, Instant.now().minusSeconds(1));
        sembrarEntrega(trabajo, instalacionB, 1, Instant.now().minusSeconds(1));

        RegistroProcesador registro = new RegistroProcesador();
        ReclamacionTrabajoPushAdapter reclamacion = adaptador(10, Duration.ofSeconds(30), registro);
        assertEquals(2, reclamacion.procesarLoteElegible().procesados());
        List<ReclamacionPush> reclamadas = new ArrayList<>(registro.reclamaciones);
        assertEquals(2, reclamadas.size());

        RegistrarResultadoEntregaPushService registrar = registrarServicio();
        CyclicBarrier barrera = new CyclicBarrier(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<Object>> resultados = reclamadas.stream().map(claim -> pool.submit(() -> {
                barrera.await(5, TimeUnit.SECONDS);
                EnvioPushPendiente envio = new EnvioPushPendiente(claim.entregaId(),
                        claim.trabajoId(), claim.instalacionId(), claim.generacion(),
                        claim.reclamacionId(), 0, Instant.now().plusSeconds(3_600),
                        Instant.now(),
                        new MensajePush(notificacion, UUID.randomUUID(),
                                com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion.NUEVA_OPORTUNIDAD,
                                "token"));
                TransactionTemplate tx = transaccion();
                tx.executeWithoutResult(estado -> registrar.ejecutar(
                        new RegistrarResultadoEntregaPushCommand(envio,
                                ResultadoEnvioPush.aceptado("fcm-" + claim.entregaId()))));
                return null;
            })).toList();
            for (Future<?> resultado : resultados) {
                resultado.get(30, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        assertEquals("FINALIZADO", estadoTrabajo(trabajo));
        assertEquals("ENTREGAS_TERMINALES", motivoTrabajo(trabajo));
        assertEquals(2, jdbc.queryForObject(
                "SELECT COUNT(*) FROM entregas_push WHERE trabajo_id = ? AND estado = 'ACEPTADA'",
                Integer.class, trabajo));
        assertEquals(0, jdbc.queryForObject(
                "SELECT COUNT(*) FROM entregas_push WHERE trabajo_id = ? AND estado = 'PENDIENTE'",
                Integer.class, trabajo));
        assertNull(jdbc.queryForObject(
                "SELECT reclamacion_id FROM trabajos_push WHERE id = ?", UUID.class, trabajo));
    }

    @Test
    void laFotoDeLaColaCuentaSoloTrabajoAbiertoYEntregasPendientes() {
        UUID prestador = sembrarUsuario("PRESTADOR");
        UUID notificacion = sembrarNotificacion(prestador);
        UUID trabajo = sembrarTrabajoPreparado(notificacion, Instant.now().plusSeconds(3_600));
        UUID instalacion = sembrarDispositivo(prestador, "token-foto");
        sembrarEntrega(trabajo, instalacion, 1, Instant.now().plusSeconds(30));

        PendientesPush conTrabajo = reclamaciones.pendientes();

        assertEquals(1, conTrabajo.trabajosAbiertos());
        assertEquals(1, conTrabajo.entregasPendientes());
        assertTrue(conTrabajo.masAntiguoEn() != null
                        && conTrabajo.antiguedadEn(Instant.now()).toMinutes() >= 59,
                "la antigüedad sale del encolado real del trabajo abierto");

        // Con la cola en reposo (padre finalizado y entrega terminal) la foto queda vacía:
        // la instrumentación no inventa pendientes.
        jdbc.update("""
                UPDATE entregas_push SET estado = 'SUPRIMIDA', intentos = 0,
                                        proximo_intento_en = NULL, finalizada_en = CURRENT_TIMESTAMP,
                                        ultimo_codigo_error = 'CANAL_NO_HABILITADO'
                WHERE trabajo_id = ?
                """, trabajo);
        jdbc.update("""
                UPDATE trabajos_push SET estado = 'FINALIZADO', finalizado_en = CURRENT_TIMESTAMP,
                                         motivo_final = 'CANAL_NO_HABILITADO'
                WHERE id = ?
                """, trabajo);

        PendientesPush vacia = reclamaciones.pendientes();

        assertEquals(0, vacia.trabajosAbiertos());
        assertEquals(0, vacia.entregasPendientes());
        assertNull(vacia.masAntiguoEn());
    }

    private ProcesadorTrabajoPushAdapter procesador(EntregaPushPort entrega, boolean preferencia,
                                                    boolean oportunidad, Duration base) {
        return procesador(entrega, preferencia, oportunidad, base, Clock.systemUTC(),
                new VentanaEnvioPush(Duration.ofSeconds(5), Duration.ofSeconds(1)));
    }

    private ProcesadorTrabajoPushAdapter procesador(EntregaPushPort entrega, boolean preferencia,
                                                    boolean oportunidad, Duration base,
                                                    Clock reloj, VentanaEnvioPush ventana) {
        PoliticaReintentoPush politica = new PoliticaReintentoPush(5, base, () -> 0.0);
        ConsultaPreferenciasPort preferencias = (usuarioId, tipoAviso, canal) -> preferencia;
        RevalidacionOportunidadPushPort revalidacion = (solicitudId, prestadorId) -> oportunidad;
        PrepararEntregasPushService preparar = new PrepararEntregasPushService(reclamaciones,
                notificaciones, dispositivos, preferencias, revalidacion,
                ConfiguracionCanalPush.desde(true, "NUEVA_OPORTUNIDAD"), metricas(reloj));
        IniciarEntregaPushService iniciar = new IniciarEntregaPushService(reclamaciones,
                notificaciones, dispositivos, preferencias, revalidacion, ventana,
                ConfiguracionCanalPush.desde(true, "NUEVA_OPORTUNIDAD"), metricas(reloj), reloj);
        RegistrarResultadoEntregaPushService registrar = new RegistrarResultadoEntregaPushService(
                reclamaciones, dispositivos, politica, metricas(reloj), reloj);
        return new ProcesadorTrabajoPushAdapter(preparar, iniciar, registrar, entrega,
                transaccion());
    }

    private RegistrarResultadoEntregaPushService registrarServicio() {
        return new RegistrarResultadoEntregaPushService(reclamaciones, dispositivos,
                new PoliticaReintentoPush(5, Duration.ofSeconds(1), () -> 0.0),
                metricas(Clock.systemUTC()), Clock.systemUTC());
    }

    private ReclamacionTrabajoPushAdapter adaptador(int maximo, Duration vigencia,
                                                    ProcesadorTrabajoPushPort procesador) {
        return new ReclamacionTrabajoPushAdapter(reclamaciones, procesador,
                metricas(Clock.systemUTC()), transaccion(), maximo, vigencia);
    }

    private MetricasPushEnMemoria metricas(Clock reloj) {
        return new MetricasPushEnMemoria(reloj);
    }

    private TransactionTemplate transaccion() {
        TransactionTemplate transaccion = new TransactionTemplate(transactionManager);
        transaccion.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transaccion;
    }

    private UUID sembrarUsuario(String rol) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO usuarios (id, email, password_hash, rol, estado_cuenta, fecha_creacion)
                VALUES (?, ?, 'x', ?, 'ACTIVA', ?)
                """, id, rol.toLowerCase() + "-" + id + "@test.local", rol, OffsetDateTime.now());
        return id;
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

    private UUID sembrarTrabajoPendiente(UUID notificacion, Instant expiraEn) {
        UUID trabajo = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO trabajos_push (id, notificacion_id, canal, estado, expira_en)
                VALUES (?, ?, 'PUSH', 'PENDIENTE_PREPARACION', ?)
                """, trabajo, notificacion, instante(expiraEn));
        return trabajo;
    }

    private UUID sembrarTrabajoPreparado(UUID notificacion, Instant expiraEn) {
        UUID trabajo = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO trabajos_push (id, notificacion_id, canal, estado, encolado_en, expira_en)
                VALUES (?, ?, 'PUSH', 'PREPARADO', CURRENT_TIMESTAMP - interval '1 hour', ?)
                """, trabajo, notificacion, instante(expiraEn));
        return trabajo;
    }

    private UUID sembrarDispositivo(UUID usuario, String token) {
        UUID instalacion = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO instalaciones_push (id, plataforma, prueba_sha256)
                VALUES (?, 'ANDROID', ?)
                """, instalacion, bytesLlenos(7));
        insertarVinculo(instalacion, 1, usuario, token);
        return instalacion;
    }

    private void insertarVinculo(UUID instalacion, long generacion, UUID usuario, String token) {
        jdbc.update("""
                INSERT INTO vinculos_push (instalacion_id, generacion, usuario_id, token_fcm,
                                           token_fcm_sha256, revocacion_sha256, vinculado_en,
                                           revocado_en)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, NULL)
                """, instalacion, generacion, usuario, token, sha256(token), bytesAleatorios());
    }

    private void rotarDispositivo(UUID instalacion, UUID usuario, long nuevaGeneracion) {
        jdbc.update("""
                UPDATE vinculos_push
                SET revocado_en = CURRENT_TIMESTAMP, token_fcm = NULL, token_fcm_sha256 = NULL
                WHERE instalacion_id = ? AND generacion = 1
                """, instalacion);
        insertarVinculo(instalacion, nuevaGeneracion, usuario, "token-rotado");
    }

    private UUID sembrarEntrega(UUID trabajo, UUID instalacion, long generacion,
                                Instant proximoIntento) {
        return sembrarEntregaConIntentos(trabajo, instalacion, generacion, proximoIntento, 0);
    }

    private UUID sembrarEntregaConIntentos(UUID trabajo, UUID instalacion, long generacion,
                                           Instant proximoIntento, int intentos) {
        UUID entrega = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO entregas_push (id, trabajo_id, instalacion_id, generacion, estado,
                                           intentos, proximo_intento_en)
                VALUES (?, ?, ?, ?, 'PENDIENTE', ?, ?)
                """, entrega, trabajo, instalacion, generacion, intentos, instante(proximoIntento));
        return entrega;
    }

    private UUID porInstalacion(UUID trabajo, UUID instalacion) {
        return jdbc.queryForObject(
                "SELECT id FROM entregas_push WHERE trabajo_id = ? AND instalacion_id = ?",
                UUID.class, trabajo, instalacion);
    }

    private Map<String, Object> filaEntrega(UUID entrega) {
        return jdbc.queryForMap("""
                SELECT estado, intentos, proximo_intento_en, id_mensaje_fcm, ultimo_codigo_error,
                       reclamacion_id, finalizada_en
                FROM entregas_push WHERE id = ?
                """, entrega);
    }

    private String estadoTrabajo(UUID trabajo) {
        return jdbc.queryForObject("SELECT estado FROM trabajos_push WHERE id = ?",
                String.class, trabajo);
    }

    private String motivoTrabajo(UUID trabajo) {
        return jdbc.queryForObject("SELECT motivo_final FROM trabajos_push WHERE id = ?",
                String.class, trabajo);
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

    /** Registra las reclamaciones para ejercer el registro de resultados en paralelo. */
    static final class RegistroProcesador implements ProcesadorTrabajoPushPort {
        final List<ReclamacionPush> reclamaciones = new CopyOnWriteArrayList<>();

        @Override
        public void procesar(ReclamacionPush reclamacion) {
            reclamaciones.add(reclamacion);
        }
    }

    /** Doble de proveedor: responde por token y cuenta los envíos sin contactar a FCM. */
    static final class EntregaPushFalsa implements EntregaPushPort {
        private final Map<String, ResultadoEnvioPush> respuestas = new ConcurrentHashMap<>();
        private final Map<String, Integer> envios = new ConcurrentHashMap<>();

        void responder(String token, ResultadoEnvioPush resultado) {
            respuestas.put(token, resultado);
        }

        int enviosA(String token) {
            return envios.getOrDefault(token, 0);
        }

        int totalEnvios() {
            return envios.values().stream().mapToInt(Integer::intValue).sum();
        }

        @Override
        public ResultadoEnvioPush enviar(MensajePush mensaje) {
            envios.merge(mensaje.tokenFcm(), 1, Integer::sum);
            return respuestas.getOrDefault(mensaje.tokenFcm(),
                    ResultadoEnvioPush.permanente("SIN_RESPUESTA"));
        }
    }
}
