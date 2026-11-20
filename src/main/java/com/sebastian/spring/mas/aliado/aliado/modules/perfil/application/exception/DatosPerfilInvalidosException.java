package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception;

public class DatosPerfilInvalidosException extends RuntimeException {
    private final String codigo;

    public DatosPerfilInvalidosException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}
