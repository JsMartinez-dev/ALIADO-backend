package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception;

public class DatosSolicitudInvalidosException extends RuntimeException {
    private final String codigo;

    public DatosSolicitudInvalidosException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String codigo() { return codigo; }
}
