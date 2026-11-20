package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception;

public class EliminacionPendienteException extends RuntimeException {
    public EliminacionPendienteException() {
        super("Ya tienes una solicitud de eliminación pendiente.");
    }
}
