package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.exception;

public class PreferenciasOperacionException extends RuntimeException {
    public enum Tipo { DATOS_INVALIDOS, PROHIBIDO, NO_ENCONTRADO, CONFLICTO }

    private final Tipo tipo;
    private final String codigo;

    public PreferenciasOperacionException(Tipo tipo, String codigo, String mensaje) {
        super(mensaje);
        this.tipo = tipo;
        this.codigo = codigo;
    }

    public Tipo tipo() {
        return tipo;
    }

    public String codigo() {
        return codigo;
    }
}
