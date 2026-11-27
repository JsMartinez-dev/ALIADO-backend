package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;
import java.util.Objects;

/** Reclamación de envío de una entrega concreta (F09-PUSH-08). */
public record IniciarEntregaPushCommand(ReclamacionPush reclamacion) {

    public IniciarEntregaPushCommand {
        Objects.requireNonNull(reclamacion, "La reclamación de envío es obligatoria");
    }
}
