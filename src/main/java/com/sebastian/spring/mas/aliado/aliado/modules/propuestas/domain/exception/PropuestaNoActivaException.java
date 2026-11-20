package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.exception;
public class PropuestaNoActivaException extends RuntimeException {
    public PropuestaNoActivaException(String estado) { super("La propuesta ya no está activa (estado " + estado + ")."); }
}
