package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Destino elegible durante la preparación (F09-PUSH-08).
 *
 * <p>Identifica la instalación y la generación del token que se usarán en la entrega.
 * No copia el token: el envío lo consulta de nuevo para revalidar la asociación.</p>
 */
public record DestinoPush(UUID instalacionId, long generacion) {

    public DestinoPush {
        Objects.requireNonNull(instalacionId, "La instalación del destino es obligatoria");
        if (generacion < 1) {
            throw new IllegalArgumentException("La generación del destino debe ser positiva");
        }
    }
}
