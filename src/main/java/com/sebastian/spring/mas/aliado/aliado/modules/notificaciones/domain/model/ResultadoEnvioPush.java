package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Resultado clasificado de un envío al proveedor push (F09-PUSH-08).
 *
 * <p>Un resultado {@link TipoResultadoEnvioPush#ACEPTADO} exige el identificador que
 * devolvió el proveedor. {@link TipoResultadoEnvioPush#TEMPORAL} puede traer la fecha de
 * {@code Retry-After}; la política de reintento usa el máximo entre ese valor y el
 * backoff con jitter. El código es un identificador corto del error, nunca un token ni
 * contenido privado.</p>
 */
public record ResultadoEnvioPush(TipoResultadoEnvioPush tipo, String idMensaje, String codigo,
                                 Instant reintentarEn) {

    public ResultadoEnvioPush {
        Objects.requireNonNull(tipo, "El tipo de resultado es obligatorio");
        if (tipo == TipoResultadoEnvioPush.ACEPTADO) {
            if (idMensaje == null || idMensaje.isBlank()) {
                throw new IllegalArgumentException("Una aceptación exige identificador del proveedor");
            }
        } else if (idMensaje != null) {
            throw new IllegalArgumentException("Solo una aceptación lleva identificador del proveedor");
        }
        if (tipo != TipoResultadoEnvioPush.TEMPORAL && reintentarEn != null) {
            throw new IllegalArgumentException("Solo un error temporal admite Retry-After");
        }
    }

    public static ResultadoEnvioPush aceptado(String idMensaje) {
        return new ResultadoEnvioPush(TipoResultadoEnvioPush.ACEPTADO, idMensaje, null, null);
    }

    public static ResultadoEnvioPush temporal(String codigo, Instant reintentarEn) {
        return new ResultadoEnvioPush(TipoResultadoEnvioPush.TEMPORAL, null, codigo, reintentarEn);
    }

    public static ResultadoEnvioPush permanente(String codigo) {
        return new ResultadoEnvioPush(TipoResultadoEnvioPush.PERMANENTE, null, codigo, null);
    }

    public static ResultadoEnvioPush tokenDesregistrado(String codigo) {
        return new ResultadoEnvioPush(TipoResultadoEnvioPush.TOKEN_DESREGISTRADO, null, codigo, null);
    }
}
