package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.IteracionTrabajoPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.MotivoDespertarTrabajo;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResultadoIteracionTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ProcesarTrabajoPushUseCase;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coordinador por instancia del worker push (F09-PUSH-06).
 *
 * <p>Un único hilo ejecuta los ciclos del caso de uso; la sesión de escucha solo deposita
 * despertares en una cola. Así nunca hay ciclos superpuestos y toda señal que llega
 * mientras se drena o se arma un plazo queda conservada para la siguiente iteración.</p>
 *
 * <p>Secuencia:</p>
 * <ol>
 *   <li>Conectar y confirmar {@code LISTEN} antes de leer la cola. Al iniciar el primer
 *       ciclo es {@link MotivoDespertarTrabajo#ARRANQUE}; tras reconectar,
 *       {@link MotivoDespertarTrabajo#RECONEXION}. Ambos recuperan el trabajo confirmado
 *       durante la desconexión.</li>
 *   <li>Drenar el lote elegible. Si el caso de uso informa que resta trabajo, iterar de
 *       inmediato con el mismo motivo, sin consultar plazos.</li>
 *   <li>Al agotar el lote, armar el único temporizador del próximo plazo persistido.
 *       Llegar una señal cancela o reemplaza ese temporizador; un vencimiento de una
 *       generación anterior se ignora.</li>
 *   <li>Sin trabajo y sin plazo, esperar bloqueado sin consultar la tabla.</li>
 *   <li>Si la sesión de escucha se pierde, cerrarla, esperar con retroceso y jitter y
 *       reconectar repitiendo la secuencia desde el paso 1.</li>
 * </ol>
 *
 * <p>La señal solo indica que puede haber trabajo: la cola persistida conserva la
 * autoridad y el caso de uso siempre vuelve a reclamar.</p>
 */
public final class CoordinadorTrabajoPush implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(CoordinadorTrabajoPush.class);

    private final ProcesarTrabajoPushUseCase ciclo;
    private final EscuchaTrabajoPush escucha;
    private final ProgramadorTemporizador temporizador;
    private final RetrocesoReconexionPush retroceso;
    private final EsperaReconexion espera;
    private final Clock reloj;

    private final BlockingQueue<Despertar> despertares = new LinkedBlockingQueue<>();
    private final AtomicLong generacionTemporizador = new AtomicLong();
    private final AtomicLong generacionEscucha = new AtomicLong();
    private final AtomicReference<Exception> falloEscucha = new AtomicReference<>();

    private volatile boolean activo;
    private volatile boolean primeraConexion = true;
    private Thread hiloCoordinador;
    private Thread hiloEscucha;
    private volatile ProgramadorTemporizador.Programacion programacion;

    public CoordinadorTrabajoPush(ProcesarTrabajoPushUseCase ciclo,
                                  EscuchaTrabajoPush escucha,
                                  ProgramadorTemporizador temporizador,
                                  RetrocesoReconexionPush retroceso,
                                  EsperaReconexion espera,
                                  Clock reloj) {
        this.ciclo = ciclo;
        this.escucha = escucha;
        this.temporizador = temporizador;
        this.retroceso = retroceso;
        this.espera = espera;
        this.reloj = reloj;
    }

    /** Arranca el hilo del coordinador; la primera lectura ocurre tras confirmar LISTEN. */
    public synchronized void iniciar() {
        if (activo) {
            return;
        }
        activo = true;
        hiloCoordinador = new Thread(this::bucle, "aliado-push-worker");
        hiloCoordinador.setDaemon(true);
        hiloCoordinador.start();
    }

    private void bucle() {
        while (activo) {
            try {
                suscribir();
                MotivoDespertarTrabajo inicial = primeraConexion
                        ? MotivoDespertarTrabajo.ARRANQUE
                        : MotivoDespertarTrabajo.RECONEXION;
                primeraConexion = false;
                retroceso.reiniciar();
                procesar(inicial);
            } catch (ConexionEscuchaPerdida perdida) {
                log.warn("Se perdió la escucha push ({}); se reconecta con retroceso y jitter",
                        perdida.getCause() == null ? perdida.getMessage() : perdida.getCause().getMessage());
                cerrarEscucha();
                if (!esperarAntesDeReconectar()) {
                    break;
                }
            } catch (RuntimeException fallo) {
                // Un fallo del ciclo no debe matar la escucha: se cierra, se retrocede y se
                // vuelve a leer la cola persistida, que conserva el trabajo.
                log.error("Fallo inesperado en el ciclo del worker push; se reconecta", fallo);
                cerrarEscucha();
                if (!esperarAntesDeReconectar()) {
                    break;
                }
            } catch (InterruptedException interrumpido) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        log.info("Coordinador del worker push detenido");
    }

    private void suscribir() {
        falloEscucha.set(null);
        try {
            escucha.abrir();
        } catch (Exception ex) {
            throw new ConexionEscuchaPerdida("No se pudo confirmar LISTEN en la conexión de escucha", ex);
        }
        long generacion = generacionEscucha.incrementAndGet();
        hiloEscucha = new Thread(() -> escuchar(generacion), "aliado-push-listen");
        hiloEscucha.setDaemon(true);
        hiloEscucha.start();
    }

    private void escuchar(long generacion) {
        Runnable recibir = () -> {
            if (generacion == generacionEscucha.get()) {
                despertares.offer(Despertar.senal());
            }
        };
        Consumer<Exception> perder = fallo -> {
            // Un callback tardío de una sesión ya reemplazada no debe forzar otra reconexión.
            if (generacion == generacionEscucha.get()) {
                falloEscucha.compareAndSet(null, fallo);
                despertares.offer(Despertar.perdidaDeConexion());
            }
        };
        try {
            escucha.escuchar(recibir, perder);
        } catch (RuntimeException ex) {
            if (generacion == generacionEscucha.get()) {
                falloEscucha.compareAndSet(null, ex);
                despertares.offer(Despertar.perdidaDeConexion());
            }
        }
    }

    private void procesar(MotivoDespertarTrabajo inicial) throws InterruptedException {
        Despertar despertar = new Despertar(inicial, 0L, false);
        while (activo) {
            comprobarConexion(despertar);
            if (despertar.esVencimientoTemporizador()
                    && despertar.generacion() != generacionTemporizador.get()) {
                // Vencimiento de un temporizador cancelado o reemplazado: no dispara ciclo.
                despertar = despertares.take();
                continue;
            }
            cancelarTemporizador();

            ResultadoIteracionTrabajoPush resultado = ciclo.ejecutar(comando(despertar.motivo()));
            Despertar siguiente = despertares.poll();
            while (siguiente == null && resultado.quedanElegibles() && activo) {
                // El lote quedó lleno: drenar de inmediato con el mismo motivo de despertar.
                resultado = ciclo.ejecutar(comando(despertar.motivo()));
                comprobarConexion(null);
                siguiente = despertares.poll();
            }
            comprobarConexion(null);
            if (!activo) {
                return;
            }

            if (siguiente != null) {
                // Una señal llegada durante el drenaje evita armar un temporizador obsoleto.
                despertar = siguiente;
                continue;
            }
            resultado.proximoPlazo().ifPresent(this::programarTemporizador);
            despertar = despertares.take();
        }
    }

    private IteracionTrabajoPushCommand comando(MotivoDespertarTrabajo motivo) {
        return new IteracionTrabajoPushCommand(motivo);
    }

    private synchronized void programarTemporizador(Instant plazo) {
        long generacion = generacionTemporizador.incrementAndGet();
        Duration retraso = Duration.between(reloj.instant(), plazo);
        if (retraso.isNegative()) {
            retraso = Duration.ZERO;
        }
        log.debug("Worker push: próximo plazo {} (generación {})", plazo, generacion);
        programacion = temporizador.programar(retraso,
                () -> despertares.offer(Despertar.vencimientoTemporizador(generacion)));
    }

    private synchronized void cancelarTemporizador() {
        // Invalidar la generación antes de cancelar descarta vencimientos en vuelo.
        generacionTemporizador.incrementAndGet();
        ProgramadorTemporizador.Programacion actual = programacion;
        programacion = null;
        if (actual != null) {
            actual.cancelar();
        }
    }

    private void comprobarConexion(Despertar despertar) {
        Exception fallo = falloEscucha.get();
        if ((despertar != null && despertar.conexionPerdida()) || fallo != null) {
            throw new ConexionEscuchaPerdida("La conexión de escucha dejó de estar activa", fallo);
        }
    }

    private boolean esperarAntesDeReconectar() {
        if (!activo) {
            return false;
        }
        try {
            espera.esperar(retroceso.siguiente());
            return activo;
        } catch (InterruptedException interrumpido) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void cerrarEscucha() {
        generacionEscucha.incrementAndGet();
        escucha.close();
        Thread hilo = hiloEscucha;
        hiloEscucha = null;
        if (hilo != null && hilo != Thread.currentThread()) {
            hilo.interrupt();
            try {
                hilo.join(1_000);
            } catch (InterruptedException interrumpido) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** Detiene el coordinador, la escucha y el temporizador. Idempotente. */
    public void cerrar() {
        activo = false;
        generacionEscucha.incrementAndGet();
        cancelarTemporizador();
        escucha.close();
        Thread escuchaActual = hiloEscucha;
        hiloEscucha = null;
        if (escuchaActual != null) {
            escuchaActual.interrupt();
        }
        Thread coordinador = hiloCoordinador;
        hiloCoordinador = null;
        if (coordinador != null && coordinador != Thread.currentThread()) {
            coordinador.interrupt();
            try {
                coordinador.join(2_000);
            } catch (InterruptedException interrumpido) {
                Thread.currentThread().interrupt();
            }
        }
        if (escuchaActual != null && escuchaActual != Thread.currentThread()) {
            try {
                escuchaActual.join(1_000);
            } catch (InterruptedException interrumpido) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** Permite usar el coordinador con try-with-resources en pruebas y composición. */
    @Override
    public void close() {
        cerrar();
    }

    private record Despertar(MotivoDespertarTrabajo motivo, long generacion, boolean conexionPerdida) {
        static Despertar senal() {
            return new Despertar(MotivoDespertarTrabajo.SENAL, 0L, false);
        }

        static Despertar perdidaDeConexion() {
            return new Despertar(MotivoDespertarTrabajo.RECONEXION, 0L, true);
        }

        static Despertar vencimientoTemporizador(long generacion) {
            return new Despertar(MotivoDespertarTrabajo.TEMPORIZADOR, generacion, false);
        }

        boolean esVencimientoTemporizador() {
            return motivo == MotivoDespertarTrabajo.TEMPORIZADOR;
        }
    }

    private static final class ConexionEscuchaPerdida extends RuntimeException {
        ConexionEscuchaPerdida(String mensaje, Exception causa) {
            super(mensaje, causa);
        }
    }
}
