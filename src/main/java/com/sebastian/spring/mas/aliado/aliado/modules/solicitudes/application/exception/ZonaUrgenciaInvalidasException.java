package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception;

/** Entrada inválida de UC017, independiente del transporte HTTP. */
public class ZonaUrgenciaInvalidasException extends RuntimeException {
    private final String codigo;

    public ZonaUrgenciaInvalidasException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}
