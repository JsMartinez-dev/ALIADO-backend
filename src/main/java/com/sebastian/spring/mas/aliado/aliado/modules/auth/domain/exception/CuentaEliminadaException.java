package com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.exception;

public class CuentaEliminadaException extends RuntimeException {
    public CuentaEliminadaException() {
        super("La cuenta fue eliminada y no puede recuperarse.");
    }
}
