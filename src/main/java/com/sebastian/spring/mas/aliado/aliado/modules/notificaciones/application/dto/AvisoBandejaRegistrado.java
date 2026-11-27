package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import java.util.Objects;
import java.util.UUID;

/**
 * Evento de aplicación emitido dentro de la transacción que registra el aviso
 * (F09-PUSH-11).
 *
 * <p>Sustituye al sondeo periódico de la tabla: la entrega del canal de bandeja se
 * dispara una sola vez, después del commit, y solo para el aviso recién creado. El
 * identificador es técnico; el resto del aviso se relee desde el repositorio.</p>
 */
public record AvisoBandejaRegistrado(UUID notificacionId) {

    public AvisoBandejaRegistrado {
        Objects.requireNonNull(notificacionId, "El aviso registrado es obligatorio");
    }
}
