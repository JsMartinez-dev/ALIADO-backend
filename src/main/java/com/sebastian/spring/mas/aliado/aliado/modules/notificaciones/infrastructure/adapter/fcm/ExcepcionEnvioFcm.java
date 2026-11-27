package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm;

import com.google.firebase.ErrorCode;
import com.google.firebase.messaging.MessagingErrorCode;
import java.time.Instant;
import java.util.Objects;

/**
 * Fallo del transporte FCM ya interpretado para el adaptador (F09-PUSH-09).
 *
 * <p>Separa el SDK del clasificador: la implementación de transporte traduce la excepción
 * de Firebase a este tipo y el adaptador decide si el resultado es temporal, permanente o
 * de token desregistrado. El mensaje de la excepción solo menciona códigos del proveedor:
 * nunca incluye el token ni contenido del aviso.</p>
 */
public class ExcepcionEnvioFcm extends Exception {

    private final MessagingErrorCode codigoMensajeria;
    private final ErrorCode codigoGeneral;
    private final Instant reintentarEn;

    public ExcepcionEnvioFcm(MessagingErrorCode codigoMensajeria, ErrorCode codigoGeneral,
                             Instant reintentarEn) {
        this(codigoMensajeria, codigoGeneral, reintentarEn, null);
    }

    public ExcepcionEnvioFcm(MessagingErrorCode codigoMensajeria, ErrorCode codigoGeneral,
                             Instant reintentarEn, Throwable causa) {
        super(describir(codigoMensajeria, codigoGeneral), causa);
        if (codigoMensajeria == null && codigoGeneral == null) {
            throw new IllegalArgumentException("El error de FCM exige al menos un código del proveedor");
        }
        this.codigoMensajeria = codigoMensajeria;
        this.codigoGeneral = codigoGeneral;
        this.reintentarEn = reintentarEn;
    }

    public MessagingErrorCode codigoMensajeria() {
        return codigoMensajeria;
    }

    public ErrorCode codigoGeneral() {
        return codigoGeneral;
    }

    /** Instante de {@code Retry-After} cuando el proveedor lo informó. */
    public Instant reintentarEn() {
        return reintentarEn;
    }

    private static String describir(MessagingErrorCode mensajeria, ErrorCode general) {
        String codigo = mensajeria != null ? mensajeria.name()
                : Objects.requireNonNull(general).name();
        return "El proveedor push no aceptó el envío: " + codigo;
    }
}
