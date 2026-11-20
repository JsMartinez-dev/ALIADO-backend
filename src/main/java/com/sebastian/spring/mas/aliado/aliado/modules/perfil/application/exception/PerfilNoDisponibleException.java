package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception;

public class PerfilNoDisponibleException extends RuntimeException {
    public PerfilNoDisponibleException() {
        super("El perfil no está disponible.");
    }
}
