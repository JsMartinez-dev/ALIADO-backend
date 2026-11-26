package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler;

import java.time.Duration;

/** Espera entre reintentos de la conexión de escucha (F09-PUSH-06). */
public interface EsperaReconexion {
    void esperar(Duration duracion) throws InterruptedException;
}
