package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception;

public class SolicitudExpiradaException extends RuntimeException {
    public SolicitudExpiradaException() {
        super("La solicitud expiró (24 h). Genera una nueva solicitud.");
    }
}
