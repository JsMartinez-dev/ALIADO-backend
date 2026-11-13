package com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.exception;

public class CuentaBloqueadaException extends RuntimeException {
    public CuentaBloqueadaException() {
        super("La cuenta está bloqueada preventivamente.");
    }
}
