package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.exception;
public class ConflictoPropuestaException extends RuntimeException {
    private final String codigo;
    public ConflictoPropuestaException(String codigo, String mensaje) { super(mensaje); this.codigo = codigo; }
    public String codigo() { return codigo; }
}
