package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception;

public class DatosUbicacionInvalidosException extends RuntimeException {
    private final String codigo;

    public DatosUbicacionInvalidosException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}
