package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;
import java.util.Objects;

/** Reclamación de preparación de un trabajo padre push (F09-PUSH-08). */
public record PrepararEntregasPushCommand(ReclamacionPush reclamacion) {

    public PrepararEntregasPushCommand {
        Objects.requireNonNull(reclamacion, "La reclamación de preparación es obligatoria");
    }
}
