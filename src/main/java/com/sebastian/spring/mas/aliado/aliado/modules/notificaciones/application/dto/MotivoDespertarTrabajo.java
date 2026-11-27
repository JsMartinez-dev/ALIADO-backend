package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

/**
 * Entrada de despertar de una iteración del worker push (F09-PUSH-06).
 *
 * <p>La señal solo indica que puede haber trabajo; la cola persistida conserva la
 * autoridad. Por eso el motivo no altera la decisión de reclamar: el caso de uso
 * siempre consulta el lote elegible.</p>
 */
public enum MotivoDespertarTrabajo {
    /** Primer ciclo tras confirmar {@code LISTEN}. */
    ARRANQUE,
    /** Llegó la señal {@code aliado_notificaciones_trabajo}. */
    SENAL,
    /** Venció el único temporizador del plazo persistido. */
    TEMPORIZADOR,
    /** Se restableció la conexión de escucha y toca releer la cola. */
    RECONEXION
}
