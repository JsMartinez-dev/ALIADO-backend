package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception;

public class SolicitudNoModificableException extends RuntimeException {
    private final String codigo;

    public SolicitudNoModificableException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String codigo() { return codigo; }
}
