package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.IteracionTrabajoPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.MotivoDespertarTrabajo;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResultadoIteracionTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ProcesarTrabajoPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.CoordinadorTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.EscuchaTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.EsperaReconexion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.ProgramadorTemporizador;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.RetrocesoReconexionPush;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-06: coordinación de despertar, drenaje, temporizador único y reconexión, con
 * dobles de reclamación, plazos y reloj controlado.
 *
 * <p>El caso de uso falso registra cada iteración con su motivo, de modo que el número de
 * iteraciones equivale al número de lecturas de cola del worker. La escucha falsa permite
 * emitir señales, simular caídas y fallos de suscripción.</p>
 */
@Timeout(30)
class CoordinadorTrabajoPushTest {

    private static final Instant AHORA = Instant.parse("2026-01-01T00:00:00Z");
    private static final Duration ESPERA = Duration.ofSeconds(3);

    private final Clock reloj = Clock.fixed(AHORA, ZoneOffset.UTC);
    private final List<String> eventos = new CopyOnWriteArrayList<>();
    private final CicloFalso ciclo = new CicloFalso(eventos);
    private final EscuchaFalsa escucha = new EscuchaFalsa(eventos);
    private final TemporizadorFalso temporizador = new TemporizadorFalso();
    private final EsperaFalsa espera = new EsperaFalsa();

    private CoordinadorTrabajoPush coordinador;

    @AfterEach
    void detener() {
        if (coordinador != null) {
            coordinador.cerrar();
        }
    }

    @Test
    void alIniciarConfirmaListenAntesDeLeerLaColaYUsaArranque() {
        arrancar(0.0);

        assertEquals(MotivoDespertarTrabajo.ARRANQUE, siguienteInvocacion().motivo());
        assertEquals(1, escucha.aperturas.get());
        int apertura = eventos.indexOf("apertura");
        int primerCiclo = eventos.indexOf("ciclo:ARRANQUE");
        assertTrue(apertura >= 0 && apertura < primerCiclo,
                "LISTEN debe confirmarse antes de leer la cola: " + eventos);
    }

    @Test
    void sinTrabajoNiPlazoEsperaSinConsultarNiArmarTemporizador() throws Exception {
        arrancar(0.0);
        assertEquals(MotivoDespertarTrabajo.ARRANQUE, siguienteInvocacion().motivo());
        assertTrue(escucha.esperarRegistro());

        Thread.sleep(250);

        assertEquals(1, ciclo.total.get(), "en reposo no se consulta la cola");
        assertTrue(temporizador.programadas.isEmpty(), "sin plazos no se arma temporizador");

        escucha.senal();
        assertEquals(MotivoDespertarTrabajo.SENAL, siguienteInvocacion().motivo());
    }

    @Test
    void drenaTodosLosLotesElegiblesAntesDeVolverAEsperar() throws Exception {
        AtomicInteger pendientes = new AtomicInteger(3);
        ciclo.respuesta = () -> pendientes.getAndDecrement() > 0
                ? ResultadoIteracionTrabajoPush.drenarDeInmediato()
                : ResultadoIteracionTrabajoPush.enReposo();
        arrancar(0.0);
        assertEquals(MotivoDespertarTrabajo.ARRANQUE, siguienteInvocacion().motivo());

        esperar(() -> ciclo.total.get() == 4);
        Thread.sleep(150);

        assertEquals(4, ciclo.total.get());
        assertEquals(List.of(MotivoDespertarTrabajo.ARRANQUE, MotivoDespertarTrabajo.ARRANQUE,
                        MotivoDespertarTrabajo.ARRANQUE, MotivoDespertarTrabajo.ARRANQUE),
                ciclo.motivos);
        assertTrue(temporizador.programadas.isEmpty(), "el drenaje no arma temporizador");
    }

    @Test
    void unaSenalDuranteElDrenajeSeConservaSinCiclosSuperpuestos() throws Exception {
        ciclo.iniciado = new CountDownLatch(1);
        ciclo.bloqueo = new CountDownLatch(1);
        arrancar(0.0);
        assertEquals(MotivoDespertarTrabajo.ARRANQUE, siguienteInvocacion().motivo());
        assertTrue(ciclo.iniciado.await(2, TimeUnit.SECONDS));
        assertTrue(escucha.esperarRegistro());

        escucha.senal();
        escucha.senal();
        ciclo.bloqueo.countDown();

        assertEquals(MotivoDespertarTrabajo.SENAL, siguienteInvocacion().motivo());
        esperar(() -> ciclo.total.get() >= 3);
        int total = ciclo.total.get();
        Thread.sleep(250);

        assertEquals(total, ciclo.total.get(), "las señales acumuladas no generan ciclos infinitos");
        assertEquals(1, ciclo.maximoEnCurso.get(), "los ciclos del worker no se solapan");
    }

    @Test
    void elTemporizadorVenceUnaVezPorPlazo() throws Exception {
        ciclo.respuesta = () -> ResultadoIteracionTrabajoPush.esperarHasta(AHORA.plusSeconds(30));
        arrancar(0.0);
        assertEquals(MotivoDespertarTrabajo.ARRANQUE, siguienteInvocacion().motivo());
        esperar(() -> temporizador.programadas.size() == 1);

        ProgramacionFalsa unica = temporizador.ultima();
        assertEquals(Duration.ofSeconds(30), unica.retraso);

        ciclo.respuesta = () -> ResultadoIteracionTrabajoPush.enReposo();
        unica.vencer();

        assertEquals(MotivoDespertarTrabajo.TEMPORIZADOR, siguienteInvocacion().motivo());
        assertEquals(2, ciclo.total.get());
        Thread.sleep(200);
        assertEquals(2, ciclo.total.get(), "un plazo vence una sola vez");
        assertTrue(unica.cancelada.get());
        assertEquals(1, temporizador.programadas.size());
    }

    @Test
    void unaSenalCancelaElTemporizadorYConservaElDespertar() throws Exception {
        ciclo.respuesta = () -> ResultadoIteracionTrabajoPush.esperarHasta(AHORA.plusSeconds(30));
        arrancar(0.0);
        assertEquals(MotivoDespertarTrabajo.ARRANQUE, siguienteInvocacion().motivo());
        esperar(() -> temporizador.programadas.size() == 1);
        ProgramacionFalsa vieja = temporizador.ultima();
        assertTrue(escucha.esperarRegistro());

        ciclo.respuesta = () -> ResultadoIteracionTrabajoPush.esperarHasta(AHORA.plusSeconds(120));
        escucha.senal();

        assertEquals(MotivoDespertarTrabajo.SENAL, siguienteInvocacion().motivo());
        esperar(() -> temporizador.programadas.size() == 2);
        assertTrue(vieja.cancelada.get(), "la señal cancela el temporizador anterior");
        assertEquals(Duration.ofSeconds(120), temporizador.ultima().retraso);

        int total = ciclo.total.get();
        vieja.vencerAunqueCancelada();
        Thread.sleep(250);
        assertEquals(total, ciclo.total.get(), "un vencimiento reemplazado no dispara un ciclo");
    }

    @Test
    void trasPerderLaConexionEsperaConRetrocesoYJitterYVuelveALeer() throws Exception {
        arrancar(0.0);
        assertEquals(MotivoDespertarTrabajo.ARRANQUE, siguienteInvocacion().motivo());
        assertTrue(escucha.esperarRegistro());

        escucha.fallarApertura = true;
        escucha.perderConexion();

        esperar(() -> espera.esperas.size() >= 2);
        assertEquals(Duration.ofMillis(100), espera.esperas.get(0));
        assertEquals(Duration.ofMillis(200), espera.esperas.get(1));
        assertTrue(escucha.aperturas.get() >= 2, "debe reintentar la suscripción");

        escucha.fallarApertura = false;
        assertEquals(MotivoDespertarTrabajo.RECONEXION, siguienteInvocacion().motivo());
        assertTrue(escucha.estaActiva(), "la sesión debe quedar suscrita tras reconectar");
    }

    @Test
    void sinSuscripcionNoLeeLaColaYUsaRetrocesoCreciente() throws Exception {
        escucha.fallarApertura = true;
        arrancar(0.0);

        esperar(() -> espera.esperas.size() >= 2);
        assertEquals(0, ciclo.total.get(), "sin LISTEN confirmado no se lee la cola");
        assertEquals(Duration.ofMillis(100), espera.esperas.get(0));
        assertEquals(Duration.ofMillis(200), espera.esperas.get(1));

        escucha.fallarApertura = false;
        assertEquals(MotivoDespertarTrabajo.ARRANQUE, siguienteInvocacion().motivo());
    }

    @Test
    void cerrarDetieneLosCiclosPosteriores() throws Exception {
        arrancar(0.0);
        assertEquals(MotivoDespertarTrabajo.ARRANQUE, siguienteInvocacion().motivo());
        int total = ciclo.total.get();

        coordinador.cerrar();
        escucha.senal();
        Thread.sleep(250);

        assertEquals(total, ciclo.total.get());
        assertFalse(escucha.estaActiva());
    }

    private void arrancar(double jitter) {
        coordinador = new CoordinadorTrabajoPush(ciclo, escucha, temporizador,
                new RetrocesoReconexionPush(Duration.ofMillis(200), Duration.ofSeconds(5), () -> jitter),
                espera, reloj);
        coordinador.iniciar();
    }

    private IteracionTrabajoPushCommand siguienteInvocacion() {
        try {
            IteracionTrabajoPushCommand command = ciclo.invocaciones.poll(ESPERA.toSeconds(), TimeUnit.SECONDS);
            assertNotNull(command, "el worker no ejecutó un ciclo a tiempo");
            return command;
        } catch (InterruptedException interrumpido) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrumpido esperando la iteración del worker", interrumpido);
        }
    }

    private static void esperar(Supplier<Boolean> condicion) throws InterruptedException {
        long limite = System.nanoTime() + ESPERA.toNanos();
        while (System.nanoTime() < limite) {
            if (Boolean.TRUE.equals(condicion.get())) {
                return;
            }
            Thread.sleep(10);
        }
        assertTrue(Boolean.TRUE.equals(condicion.get()), "la condición no se cumplió a tiempo");
    }

    static final class CicloFalso implements ProcesarTrabajoPushUseCase {
        final BlockingQueue<IteracionTrabajoPushCommand> invocaciones = new LinkedBlockingQueue<>();
        final List<MotivoDespertarTrabajo> motivos = new CopyOnWriteArrayList<>();
        final AtomicInteger total = new AtomicInteger();
        final AtomicInteger enCurso = new AtomicInteger();
        final AtomicInteger maximoEnCurso = new AtomicInteger();
        private final List<String> eventos;

        volatile Supplier<ResultadoIteracionTrabajoPush> respuesta = ResultadoIteracionTrabajoPush::enReposo;
        volatile CountDownLatch bloqueo;
        volatile CountDownLatch iniciado;

        CicloFalso(List<String> eventos) {
            this.eventos = eventos;
        }

        @Override
        public ResultadoIteracionTrabajoPush ejecutar(IteracionTrabajoPushCommand command) {
            int actual = enCurso.incrementAndGet();
            maximoEnCurso.accumulateAndGet(actual, Math::max);
            total.incrementAndGet();
            motivos.add(command.motivo());
            eventos.add("ciclo:" + command.motivo());
            invocaciones.add(command);
            if (iniciado != null) {
                iniciado.countDown();
            }
            try {
                CountDownLatch traba = bloqueo;
                if (traba != null) {
                    traba.await(5, TimeUnit.SECONDS);
                }
                return respuesta.get();
            } catch (InterruptedException interrumpido) {
                Thread.currentThread().interrupt();
                return ResultadoIteracionTrabajoPush.enReposo();
            } finally {
                enCurso.decrementAndGet();
            }
        }
    }

    static final class EscuchaFalsa implements EscuchaTrabajoPush {
        final AtomicInteger aperturas = new AtomicInteger();
        private final List<String> eventos;
        private volatile Runnable alRecibir;
        private volatile Consumer<Exception> alPerder;
        private volatile boolean activa;
        private volatile CountDownLatch registrado = new CountDownLatch(1);
        private volatile CountDownLatch cierre = new CountDownLatch(1);

        volatile boolean fallarApertura;

        EscuchaFalsa(List<String> eventos) {
            this.eventos = eventos;
        }

        @Override
        public void abrir() throws Exception {
            aperturas.incrementAndGet();
            eventos.add("apertura");
            if (fallarApertura) {
                throw new SQLException("suscripción simulada fallida");
            }
            activa = true;
            registrado = new CountDownLatch(1);
            cierre = new CountDownLatch(1);
        }

        @Override
        public void escuchar(Runnable alRecibirSenal, Consumer<Exception> alPerderConexion) {
            this.alRecibir = alRecibirSenal;
            this.alPerder = alPerderConexion;
            registrado.countDown();
            try {
                cierre.await();
            } catch (InterruptedException interrumpido) {
                Thread.currentThread().interrupt();
            }
        }

        boolean esperarRegistro() throws InterruptedException {
            return registrado.await(ESPERA.toSeconds(), TimeUnit.SECONDS);
        }

        void senal() {
            Runnable receptor = alRecibir;
            assertNotNull(receptor, "la escucha debe estar registrada antes de emitir una señal");
            receptor.run();
        }

        void perderConexion() {
            activa = false;
            Consumer<Exception> perdida = alPerder;
            assertNotNull(perdida, "la escucha debe estar registrada antes de simular la caída");
            perdida.accept(new SQLException("caída simulada"));
        }

        @Override
        public boolean estaActiva() {
            return activa;
        }

        @Override
        public void close() {
            activa = false;
            cierre.countDown();
        }
    }

    static final class TemporizadorFalso implements ProgramadorTemporizador {
        final List<ProgramacionFalsa> programadas = new CopyOnWriteArrayList<>();

        @Override
        public Programacion programar(Duration retraso, Runnable tarea) {
            ProgramacionFalsa programacion = new ProgramacionFalsa(retraso, tarea);
            programadas.add(programacion);
            return programacion;
        }

        ProgramacionFalsa ultima() {
            return programadas.get(programadas.size() - 1);
        }

        @Override
        public void close() {
            // Sin recursos propios.
        }
    }

    static final class ProgramacionFalsa implements ProgramadorTemporizador.Programacion {
        final Duration retraso;
        final AtomicBoolean cancelada = new AtomicBoolean();
        private final Runnable tarea;

        ProgramacionFalsa(Duration retraso, Runnable tarea) {
            this.retraso = retraso;
            this.tarea = tarea;
        }

        @Override
        public void cancelar() {
            cancelada.set(true);
        }

        void vencer() {
            if (!cancelada.get()) {
                tarea.run();
            }
        }

        void vencerAunqueCancelada() {
            tarea.run();
        }
    }

    static final class EsperaFalsa implements EsperaReconexion {
        final List<Duration> esperas = new CopyOnWriteArrayList<>();

        @Override
        public void esperar(Duration duracion) {
            esperas.add(duracion);
        }
    }
}
