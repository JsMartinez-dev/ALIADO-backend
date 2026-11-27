package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.EntregarAvisoBandejaCommand;

/**
 * Entrega del canal de bandeja para un aviso recién registrado (F09-PUSH-11).
 *
 * <p>Sustituye al {@code @Scheduled} de 60 segundos: el aviso se entrega una vez, por el
 * evento de registro confirmado, y nunca por un barrido periódico de la tabla.</p>
 */
public interface EntregarAvisoBandejaUseCase {
    void ejecutar(EntregarAvisoBandejaCommand command);
}
