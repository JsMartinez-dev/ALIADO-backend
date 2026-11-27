package com.sebastian.spring.mas.aliado.aliado.modules.perfil.infrastructure.scheduler;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.LimpiarArchivosPendientesUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LimpiezaArchivosPendientesScheduler {
    private final LimpiarArchivosPendientesUseCase limpieza;

    public LimpiezaArchivosPendientesScheduler(LimpiarArchivosPendientesUseCase limpieza) {
        this.limpieza = limpieza;
    }

    @Scheduled(fixedDelayString = "${aliado.file-cleanup.retry-ms:300000}")
    public void reintentar() {
        limpieza.ejecutar(null);
    }
}
