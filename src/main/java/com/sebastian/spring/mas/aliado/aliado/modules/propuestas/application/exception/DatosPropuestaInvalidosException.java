package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.exception;
public class DatosPropuestaInvalidosException extends RuntimeException {
    private final String codigo;
    public DatosPropuestaInvalidosException(String codigo, String mensaje) { super(mensaje); this.codigo = codigo; }
    public String codigo() { return codigo; }
}
