package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.exception;
public class RecursoPropuestaNoEncontradoException extends RuntimeException {
    private final String codigo;
    public RecursoPropuestaNoEncontradoException(String codigo, String mensaje) { super(mensaje); this.codigo = codigo; }
    public String codigo() { return codigo; }
}
