package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

/**
 * Elemento que el worker reclama de la cola push (F09-PUSH-07).
 *
 * <p>La preparación reclama un trabajo padre {@code PENDIENTE_PREPARACION}; el envío
 * reclama una entrega {@code PENDIENTE} cuyo {@code proximo_intento_en} ya venció.</p>
 */
public enum TipoReclamacionPush {
    PREPARACION,
    ENVIO
}
