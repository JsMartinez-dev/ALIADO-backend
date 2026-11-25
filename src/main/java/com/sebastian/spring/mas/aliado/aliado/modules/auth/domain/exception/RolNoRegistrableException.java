package com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.exception;

public class RolNoRegistrableException extends RuntimeException {
    public RolNoRegistrableException() {
        super("El rol ADMINISTRADOR no se registra por la API pública.");
    }
}
