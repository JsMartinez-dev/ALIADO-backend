package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

/**
 * Estados del trabajo padre push de V9 (F09-PUSH-05/07/08).
 *
 * <p>{@link #PENDIENTE_PREPARACION} admite reclamación con identificador y vencimiento.
 * {@link #PREPARADO} ya tiene sus entregas y solo termina cuando ninguna sigue
 * {@link EstadoEntregaPush#PENDIENTE}. {@link #FINALIZADO} es terminal y conserva el
 * motivo del cierre.</p>
 */
public enum EstadoTrabajoPush {
    PENDIENTE_PREPARACION,
    PREPARADO,
    FINALIZADO
}
