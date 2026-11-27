package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

/**
 * Clasificación del resultado del proveedor push (F09-PUSH-08/09).
 *
 * <p>La política de reintentos vive en la aplicación: el adaptador solo traduce la
 * respuesta del proveedor a una de estas categorías.</p>
 */
public enum TipoResultadoEnvioPush {
    /** El proveedor aceptó el mensaje y devolvió un identificador. */
    ACEPTADO,
    /** Error de red, cuota o indisponibilidad: se puede reintentar el mismo dispositivo. */
    TEMPORAL,
    /** Error permanente del mensaje o de configuración: no se reintenta. */
    PERMANENTE,
    /** El token ya no está registrado: se revoca solo la generación enviada. */
    TOKEN_DESREGISTRADO
}
