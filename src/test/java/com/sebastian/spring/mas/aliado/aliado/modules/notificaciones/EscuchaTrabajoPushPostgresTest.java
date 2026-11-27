package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.IteracionTrabajoPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.MotivoDespertarTrabajo;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResultadoIteracionTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ProcesarTrabajoPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.SenalTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.CoordinadorTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.EscuchaTrabajoPushPostgres;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.EsperaReconexionHilo;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.ProgramadorTemporizadorEjecutor;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.RetrocesoReconexionPush;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-06 sobre PostgreSQL real: confirmación de {@code LISTEN} antes de leer,
 * entrega de la señal solo al confirmar, reapertura tras perder la sesión, recuperación
 * del trabajo confirmado al arrancar, señales durante el drenaje y reposo instrumentado
 * de más de 60 segundos.
 *
 * <p>El caso de uso se sustituye por un doble porque la reclamación SQL llega en
 * F09-PUSH-07. Cada iteración del doble equivale a una lectura de cola del worker: el
 * contador de reposo mide exactamente esas lecturas y no las consultas de la suite.</p>
 *
 * <p>Solo se ejecuta con {@code ALIADO_PG_URL} definido y una base desechable.</p>
 */
@EnabledIfEnvironmentVariable(named = "ALIADO_PG_URL", matches = ".+")
@Timeout(180)
@SpringBootTest
class EscuchaTrabajoPushPostgresTest {

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

    private final List<Thread> hilos = new ArrayList<>();
    private final List<AutoCloseable> recursos = new ArrayList<>();

    @AfterEach
    void cerrarRecursos() {
        for (AutoCloseable recurso : recursos) {
            try {
                recurso.close();
            } catch (Exception ignorada) {
                // Cierre best-effort en la limpieza.
            }
        }
        for (Thread hilo : hilos) {
            hilo.interrupt();
        }
        recursos.clear();
        hilos.clear();
    }

    @Test
    void confirmaListenYEntregaLaSenalSoloAlConfirmarLaTransaccionEmisora() throws Exception {
        EscuchaTrabajoPushPostgres escucha = nuevaEscucha();
        escucha.abrir();
        assertTrue(escucha.estaActiva(), "LISTEN debe quedar confirmado al abrir");

        AtomicInteger senales = new AtomicInteger();
        CountDownLatch primera = new CountDownLatch(1);
        AtomicReference<Exception> perdida = new AtomicReference<>();
        escucharEnHilo(escucha, senales, primera, perdida);

        try (Connection emisora = abrirConexion()) {
            emisora.setAutoCommit(false);
            try (Statement sentencia = emisora.createStatement()) {
                sentencia.execute("SELECT pg_notify('" + SenalTrabajoPush.CANAL + "', '')");
            }
            assertFalse(primera.await(800, TimeUnit.MILLISECONDS),
                    "NOTIFY no se entrega antes del commit");
            assertEquals(0, senales.get());
            emisora.commit();
        }

        assertTrue(primera.await(5, TimeUnit.SECONDS), "el commit debe despertar a la escucha");
        assertEquals(1, senales.get());
        assertNull(perdida.get());
    }

    @Test
    void reabreLaSuscripcionTrasPerderLaSesion() throws Exception {
        EscuchaTrabajoPushPostgres escucha = nuevaEscucha();
        escucha.abrir();

        AtomicInteger senales = new AtomicInteger();
        CountDownLatch primera = new CountDownLatch(1);
        CountDownLatch caida = new CountDownLatch(1);
        AtomicReference<Exception> perdida = new AtomicReference<>();
        escucharEnHilo(escucha, senales, primera, perdida, caida);

        Integer pid = jdbc.queryForObject("""
                SELECT pid FROM pg_stat_activity
                WHERE application_name = 'aliado-push-listen'
                  AND datname = current_database()
                  AND pid <> pg_backend_pid()
                ORDER BY backend_start DESC LIMIT 1
                """, Integer.class);
        assertNotNull(pid, "debe existir la sesión de escucha dedicada");
        jdbc.queryForObject("SELECT pg_terminate_backend(?)", Boolean.class, pid);

        assertTrue(caida.await(10, TimeUnit.SECONDS), "la pérdida de sesión debe notificarse");
        assertFalse(escucha.estaActiva());

        escucha.abrir();
        assertTrue(escucha.estaActiva());
        AtomicInteger senalesReconectada = new AtomicInteger();
        CountDownLatch recibida = new CountDownLatch(1);
        escucharEnHilo(escucha, senalesReconectada, recibida, new AtomicReference<>());

        assertTrue(notificarHastaRecibir(recibida), "tras reconectar debe recibir la señal");
        assertTrue(senalesReconectada.get() >= 1);
    }

    @Test
    void elArranqueConTrabajoConfirmadoLoLeeDespuesDeConfirmarListen() throws Exception {
        UUID trabajo = sembrarTrabajoConfirmado();
        CicloEspia ciclo = new CicloEspia(() -> jdbc.queryForObject(
                "SELECT COUNT(*) FROM trabajos_push WHERE id = ?", Integer.class, trabajo));
        try (CoordinadorTrabajoPush coordinador = coordinador(ciclo, nuevaEscucha())) {
            coordinador.iniciar();

            IteracionTrabajoPushCommand primera = ciclo.siguienteInvocacion();
            assertEquals(MotivoDespertarTrabajo.ARRANQUE, primera.motivo());
            assertEquals(1, ciclo.trabajosVistos.get(0),
                    "el primer ciclo debe ver el trabajo confirmado antes de arrancar");
        }
    }

    @Test
    void lasSenalesDuranteElDrenajeSeConservanYNoSolapanCiclos() throws Exception {
        CicloEspia ciclo = new CicloEspia(() -> 0);
        ciclo.iniciado = new CountDownLatch(1);
        ciclo.bloqueo = new CountDownLatch(1);

        try (CoordinadorTrabajoPush coordinador = coordinador(ciclo, nuevaEscucha())) {
            coordinador.iniciar();
            assertEquals(MotivoDespertarTrabajo.ARRANQUE, ciclo.siguienteInvocacion().motivo());
            assertTrue(ciclo.iniciado.await(5, TimeUnit.SECONDS));

            notificar();
            notificar();
            ciclo.bloqueo.countDown();

            assertEquals(MotivoDespertarTrabajo.SENAL, ciclo.siguienteInvocacion().motivo());
            esperar(() -> ciclo.total.get() >= 2);
            Thread.sleep(300);
            assertEquals(1, ciclo.maximoEnCurso.get(), "los ciclos no se solapan");
        }
    }

    @Test
    void elPlazoPersistidoDespiertaAlWorkerSinNuevaSenal() throws Exception {
        CicloEspia ciclo = new CicloEspia(() -> 0);
        ciclo.respuesta = () -> ResultadoIteracionTrabajoPush.esperarHasta(Instant.now().plusSeconds(1));

        try (CoordinadorTrabajoPush coordinador = coordinador(ciclo, nuevaEscucha())) {
            coordinador.iniciar();
            assertEquals(MotivoDespertarTrabajo.ARRANQUE, ciclo.siguienteInvocacion().motivo());

            ciclo.respuesta = ResultadoIteracionTrabajoPush::enReposo;
            assertEquals(MotivoDespertarTrabajo.TEMPORIZADOR, ciclo.siguienteInvocacion().motivo());
        }
    }

    @Test
    void conColaVaciaNoConsultaLaColaDuranteMasDeSesentaSegundos() throws Exception {
        CicloEspia ciclo = new CicloEspia(() -> 0);
        try (CoordinadorTrabajoPush coordinador = coordinador(ciclo, nuevaEscucha())) {
            coordinador.iniciar();
            assertEquals(MotivoDespertarTrabajo.ARRANQUE, ciclo.siguienteInvocacion().motivo());
            int lecturasIniciales = ciclo.total.get();

            long inicio = System.nanoTime();
            Thread.sleep(65_000);
            long transcurridoMs = (System.nanoTime() - inicio) / 1_000_000;

            assertTrue(transcurridoMs >= 60_000, "la ventana de reposo debe superar los 60 s");
            assertEquals(lecturasIniciales, ciclo.total.get(),
                    "con cola vacía y sin plazo no se consulta la cola");

            notificar();
            esperar(() -> ciclo.total.get() == lecturasIniciales + 1);
            IteracionTrabajoPushCommand despertar = ciclo.invocaciones.poll(2, TimeUnit.SECONDS);
            assertNotNull(despertar, "la señal debe despertar al worker");
            assertEquals(MotivoDespertarTrabajo.SENAL, despertar.motivo());
        }
    }

    private EscuchaTrabajoPushPostgres nuevaEscucha() {
        EscuchaTrabajoPushPostgres escucha = new EscuchaTrabajoPushPostgres(
                System.getenv("ALIADO_PG_URL"), leer("ALIADO_PG_USERNAME", "aliado"),
                leer("ALIADO_PG_PASSWORD", "aliado"), SenalTrabajoPush.CANAL);
        recursos.add(escucha);
        return escucha;
    }

    private CoordinadorTrabajoPush coordinador(ProcesarTrabajoPushUseCase ciclo,
                                               EscuchaTrabajoPushPostgres escucha) {
        ProgramadorTemporizadorEjecutor temporizador = new ProgramadorTemporizadorEjecutor();
        recursos.add(temporizador);
        return new CoordinadorTrabajoPush(ciclo, escucha, temporizador,
                new RetrocesoReconexionPush(Duration.ofMillis(50), Duration.ofMillis(1_000)),
                new EsperaReconexionHilo(), Clock.systemUTC());
    }

    private void escucharEnHilo(EscuchaTrabajoPushPostgres escucha, AtomicInteger senales,
                                CountDownLatch primera, AtomicReference<Exception> perdida) {
        escucharEnHilo(escucha, senales, primera, perdida, new CountDownLatch(1));
    }

    private void escucharEnHilo(EscuchaTrabajoPushPostgres escucha, AtomicInteger senales,
                                CountDownLatch primera, AtomicReference<Exception> perdida,
                                CountDownLatch caida) {
        Thread hilo = new Thread(() -> escucha.escuchar(() -> {
            senales.incrementAndGet();
            primera.countDown();
        }, ex -> {
            perdida.set(ex);
            caida.countDown();
        }), "escucha-postgres-test");
        hilo.setDaemon(true);
        hilo.start();
        hilos.add(hilo);
    }

    private boolean notificarHastaRecibir(CountDownLatch recibida) throws SQLException, InterruptedException {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < limite) {
            notificar();
            if (recibida.await(200, TimeUnit.MILLISECONDS)) {
                return true;
            }
        }
        return false;
    }

    private void notificar() throws SQLException {
        try (Connection conexion = abrirConexion();
             Statement sentencia = conexion.createStatement()) {
            sentencia.execute("SELECT pg_notify('" + SenalTrabajoPush.CANAL + "', '')");
        }
    }

    private Connection abrirConexion() throws SQLException {
        return DriverManager.getConnection(System.getenv("ALIADO_PG_URL"),
                leer("ALIADO_PG_USERNAME", "aliado"), leer("ALIADO_PG_PASSWORD", "aliado"));
    }

    private UUID sembrarTrabajoConfirmado() {
        UUID usuario = UUID.randomUUID();
        UUID notificacion = UUID.randomUUID();
        UUID trabajo = UUID.randomUUID();
        jdbc.update("INSERT INTO usuarios (id, email, password_hash, rol, estado_cuenta, fecha_creacion) "
                        + "VALUES (?, ?, 'x', 'PRESTADOR', 'ACTIVA', ?)",
                usuario, "prestador-" + usuario + "@test.local", OffsetDateTime.now());
        jdbc.update("INSERT INTO notificaciones (id, destinatario_id, tipo, aggregate_id, event_version, "
                        + "detalle, canal, estado_entrega, intentos, fecha_generacion) "
                        + "VALUES (?, ?, 'NUEVA_OPORTUNIDAD', ?, 1, 'detalle', 'PUSH', 'PENDIENTE', 0, ?)",
                notificacion, usuario, UUID.randomUUID(), OffsetDateTime.now());
        jdbc.update("INSERT INTO trabajos_push (id, notificacion_id, canal, estado, expira_en) "
                        + "VALUES (?, ?, 'PUSH', 'PENDIENTE_PREPARACION', CURRENT_TIMESTAMP + INTERVAL '1 hour')",
                trabajo, notificacion);
        return trabajo;
    }

    private static void esperar(Supplier<Boolean> condicion) throws InterruptedException {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < limite) {
            if (Boolean.TRUE.equals(condicion.get())) {
                return;
            }
            Thread.sleep(20);
        }
        assertTrue(Boolean.TRUE.equals(condicion.get()), "la condición no se cumplió a tiempo");
    }

    private static String leer(String variable, String porDefecto) {
        String valor = System.getenv(variable);
        return valor == null || valor.isBlank() ? porDefecto : valor;
    }

    static final class CicloEspia implements ProcesarTrabajoPushUseCase {
        final BlockingQueue<IteracionTrabajoPushCommand> invocaciones = new LinkedBlockingQueue<>();
        final List<MotivoDespertarTrabajo> motivos = new CopyOnWriteArrayList<>();
        final List<Integer> trabajosVistos = new CopyOnWriteArrayList<>();
        final AtomicInteger total = new AtomicInteger();
        final AtomicInteger enCurso = new AtomicInteger();
        final AtomicInteger maximoEnCurso = new AtomicInteger();
        private final IntSupplier trabajosVisibles;

        volatile Supplier<ResultadoIteracionTrabajoPush> respuesta = ResultadoIteracionTrabajoPush::enReposo;
        volatile CountDownLatch bloqueo;
        volatile CountDownLatch iniciado;

        CicloEspia(IntSupplier trabajosVisibles) {
            this.trabajosVisibles = trabajosVisibles;
        }

        @Override
        public ResultadoIteracionTrabajoPush ejecutar(IteracionTrabajoPushCommand command) {
            int actual = enCurso.incrementAndGet();
            maximoEnCurso.accumulateAndGet(actual, Math::max);
            total.incrementAndGet();
            motivos.add(command.motivo());
            trabajosVistos.add(trabajosVisibles.getAsInt());
            invocaciones.add(command);
            if (iniciado != null) {
                iniciado.countDown();
            }
            try {
                CountDownLatch traba = bloqueo;
                if (traba != null) {
                    traba.await(10, TimeUnit.SECONDS);
                }
                return respuesta.get();
            } catch (InterruptedException interrumpido) {
                Thread.currentThread().interrupt();
                return ResultadoIteracionTrabajoPush.enReposo();
            } finally {
                enCurso.decrementAndGet();
            }
        }

        IteracionTrabajoPushCommand siguienteInvocacion() throws InterruptedException {
            IteracionTrabajoPushCommand command = invocaciones.poll(5, TimeUnit.SECONDS);
            assertNotNull(command, "el worker no ejecutó un ciclo a tiempo");
            return command;
        }
    }
}
