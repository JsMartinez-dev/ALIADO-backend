package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

/**
 * Motivos de cierre del trabajo padre push (V9, F09-PUSH-08).
 *
 * <p>Se persisten en {@code trabajos_push.motivo_final}. El cierre por entregas
 * terminales no distingue resultados: cada entrega conserva su propio estado y código.</p>
 */
public enum MotivoCierreTrabajoPush {
    SIN_DISPOSITIVOS_ELEGIBLES,
    PREFERENCIA_DESHABILITADA,
    OPORTUNIDAD_NO_VIGENTE,
    TIPO_NO_SOPORTADO,
    VIGENCIA_AGOTADA,
    ENTREGAS_TERMINALES
}
