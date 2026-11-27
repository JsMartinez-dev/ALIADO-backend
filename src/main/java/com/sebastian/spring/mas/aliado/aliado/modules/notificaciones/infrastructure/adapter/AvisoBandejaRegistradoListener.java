package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.AvisoBandejaRegistrado;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.EntregarAvisoBandejaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.EntregarAvisoBandejaUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Dispara la entrega del canal de bandeja al confirmar el registro del aviso
 * (F09-PUSH-11).
 *
 * <p>Reemplaza al {@code @Scheduled(fixedDelay = 60_000)}: no existe ningún sondeo, la
 * entrega ocurre una sola vez por aviso y únicamente cuando la transacción de negocio
 * confirma. Con {@code fallbackExecution} también atiende registros sin transacción
 * explícita, que el caso de uso resuelve en su propia transacción.</p>
 */
@Component
public class AvisoBandejaRegistradoListener {

    private final EntregarAvisoBandejaUseCase entregarAviso;

    public AvisoBandejaRegistradoListener(EntregarAvisoBandejaUseCase entregarAviso) {
        this.entregarAviso = entregarAviso;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alRegistrarse(AvisoBandejaRegistrado evento) {
        entregarAviso.ejecutar(new EntregarAvisoBandejaCommand(evento.notificacionId()));
    }
}
