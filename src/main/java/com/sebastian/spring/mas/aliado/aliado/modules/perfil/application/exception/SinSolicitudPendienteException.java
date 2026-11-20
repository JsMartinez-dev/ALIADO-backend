package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception;

public class SinSolicitudPendienteException extends RuntimeException {
    public SinSolicitudPendienteException() {
        super("No tienes una solicitud de eliminación pendiente.");
    }
}
