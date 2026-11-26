package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

/**
 * Estados de una entrega por dispositivo de V9 (F09-PUSH-07).
 *
 * <p>{@link #PENDIENTE} conserva el reintento con su {@code proximo_intento_en}; el resto
 * son terminales. No confundir con {@link EstadoEntrega}, que describe el estado del
 * aviso unificado de la bandeja.</p>
 */
public enum EstadoEntregaPush {
    PENDIENTE,
    ACEPTADA,
    FALLIDA_DEFINITIVA,
    SUPRIMIDA,
    EXPIRADA
}
