package com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.exception;

public class PerfilPublicoNoDisponibleException extends RuntimeException {
    public PerfilPublicoNoDisponibleException() {
        super("El perfil no está disponible.");
    }
}

