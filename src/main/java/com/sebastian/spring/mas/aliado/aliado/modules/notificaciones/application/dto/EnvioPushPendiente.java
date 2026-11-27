package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.MensajePush;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Envío ya revalidado, listo para llamar al proveedor (F09-PUSH-08).
 *
 * <p>Conserva el contexto necesario para guardar el resultado condicionado a la
 * reclamación y a la generación del token reclamada. No contiene el aviso completo:
 * solo el mensaje mínimo y los plazos. {@code encoladoEn} permite medir la latencia
 * hasta la aceptación del proveedor (F09-PUSH-11).</p>
 */
public record EnvioPushPendiente(UUID entregaId, UUID trabajoId, UUID instalacionId,
                                 long generacion, UUID reclamacionId, int intentos,
                                 Instant expiraEn, Instant encoladoEn, MensajePush mensaje) {

    public EnvioPushPendiente {
        Objects.requireNonNull(entregaId, "La entrega a enviar es obligatoria");
        Objects.requireNonNull(trabajoId, "El trabajo a enviar es obligatorio");
        Objects.requireNonNull(instalacionId, "La instalación a enviar es obligatoria");
        Objects.requireNonNull(reclamacionId, "La reclamación del envío es obligatoria");
        Objects.requireNonNull(expiraEn, "La vigencia del envío es obligatoria");
        Objects.requireNonNull(encoladoEn, "El encolado del envío es obligatorio");
        Objects.requireNonNull(mensaje, "El mensaje del envío es obligatorio");
        if (generacion < 1) {
            throw new IllegalArgumentException("La generación del envío debe ser positiva");
        }
        if (intentos < 0) {
            throw new IllegalArgumentException("Los intentos del envío no pueden ser negativos");
        }
    }
}
