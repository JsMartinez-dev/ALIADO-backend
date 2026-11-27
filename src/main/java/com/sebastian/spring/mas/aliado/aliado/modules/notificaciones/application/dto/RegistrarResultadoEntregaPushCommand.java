package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEnvioPush;
import java.util.Objects;

/** Resultado del proveedor que debe aplicarse a una entrega reclamada (F09-PUSH-08). */
public record RegistrarResultadoEntregaPushCommand(EnvioPushPendiente envio,
                                                   ResultadoEnvioPush resultado) {

    public RegistrarResultadoEntregaPushCommand {
        Objects.requireNonNull(envio, "El envío del resultado es obligatorio");
        Objects.requireNonNull(resultado, "El resultado del proveedor es obligatorio");
    }
}
