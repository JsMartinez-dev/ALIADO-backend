package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler;

import java.time.Duration;

/** Espera real entre reintentos de la conexión de escucha (F09-PUSH-06). */
public final class EsperaReconexionHilo implements EsperaReconexion {
    @Override
    public void esperar(Duration duracion) throws InterruptedException {
        Thread.sleep(Math.max(0L, duracion.toMillis()));
    }
}
