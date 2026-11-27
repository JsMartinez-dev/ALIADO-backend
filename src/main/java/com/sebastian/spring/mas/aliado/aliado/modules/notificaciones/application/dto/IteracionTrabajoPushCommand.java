package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import java.util.Objects;

/**
 * Entrada de una iteración del worker push (F09-PUSH-06).
 *
 * <p>El motivo documenta qué despertó al worker; no altera la decisión de reclamar porque
 * la cola persistida conserva la autoridad y la hora de PostgreSQL decide la
 * elegibilidad. El plazo que devuelve la iteración se calcula con el reloj del
 * coordinador.</p>
 *
 * @param motivo por qué se despertó al worker
 */
public record IteracionTrabajoPushCommand(MotivoDespertarTrabajo motivo) {
    public IteracionTrabajoPushCommand {
        Objects.requireNonNull(motivo, "motivo");
    }
}
