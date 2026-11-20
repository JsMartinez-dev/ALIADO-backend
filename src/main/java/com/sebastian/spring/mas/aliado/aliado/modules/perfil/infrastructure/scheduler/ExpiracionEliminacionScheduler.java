package com.sebastian.spring.mas.aliado.aliado.modules.perfil.infrastructure.scheduler;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ExpirarSolicitudesEliminacionUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Marca como EXPIRADA cada hora las solicitudes de eliminación pendientes con más de 24 h (SPEC-02). */
@Component
public class ExpiracionEliminacionScheduler {
    private final ExpirarSolicitudesEliminacionUseCase expirarSolicitudes;

    public ExpiracionEliminacionScheduler(ExpirarSolicitudesEliminacionUseCase expirarSolicitudes) {
        this.expirarSolicitudes = expirarSolicitudes;
    }

    @Scheduled(fixedDelay = 3_600_000)
    public void expirarVencidas() {
        expirarSolicitudes.ejecutar();
    }
}
