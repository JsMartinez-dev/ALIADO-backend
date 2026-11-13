package com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.exception;

public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Credenciales inválidas.");
    }
}
