package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Temporizador del worker sobre un ejecutor de un solo hilo (F09-PUSH-06). Cada plazo
 * programa exactamente una tarea; el coordinador cancela la anterior antes de armar la
 * siguiente.
 */
public final class ProgramadorTemporizadorEjecutor implements ProgramadorTemporizador {
    private final ScheduledExecutorService ejecutor;

    public ProgramadorTemporizadorEjecutor() {
        this(Executors.newSingleThreadScheduledExecutor(tarea -> {
            Thread hilo = new Thread(tarea, "aliado-push-timer");
            hilo.setDaemon(true);
            return hilo;
        }));
    }

    ProgramadorTemporizadorEjecutor(ScheduledExecutorService ejecutor) {
        this.ejecutor = ejecutor;
    }

    @Override
    public Programacion programar(Duration retraso, Runnable tarea) {
        long milisegundos = Math.max(0L, retraso.toMillis());
        ScheduledFuture<?> futuro = ejecutor.schedule(tarea, milisegundos, TimeUnit.MILLISECONDS);
        return () -> futuro.cancel(false);
    }

    @Override
    public void close() {
        ejecutor.shutdownNow();
    }
}
