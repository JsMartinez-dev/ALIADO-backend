package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception;

public class CatalogoPerfilInvalidoException extends RuntimeException {
    private final String codigo;

    public CatalogoPerfilInvalidoException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String codigo() { return codigo; }
}
