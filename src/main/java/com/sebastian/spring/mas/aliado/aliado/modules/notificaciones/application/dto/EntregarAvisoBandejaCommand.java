package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import java.util.Objects;
import java.util.UUID;

/** Aviso de bandeja cuya entrega por canal se intenta tras el commit (F09-PUSH-11). */
public record EntregarAvisoBandejaCommand(UUID notificacionId) {

    public EntregarAvisoBandejaCommand {
        Objects.requireNonNull(notificacionId, "El aviso a entregar es obligatorio");
    }
}
