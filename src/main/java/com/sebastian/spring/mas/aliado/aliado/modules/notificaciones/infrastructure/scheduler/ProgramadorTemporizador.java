package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler;

import java.time.Duration;

/**
 * Único temporizador del worker push (F09-PUSH-06).
 *
 * <p>El coordinador programa a lo sumo una ejecución por plazo y cancela o reemplaza la
 * anterior. La abstracción permite probar con un temporizador manual y reloj controlado
 * sin depender de esperas reales.</p>
 */
public interface ProgramadorTemporizador extends AutoCloseable {

    Programacion programar(Duration retraso, Runnable tarea);

    @Override
    void close();

    /** Ejecución programada cancelable. */
    interface Programacion {
        void cancelar();
    }
}
