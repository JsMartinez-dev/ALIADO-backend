package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception;

public class PerfilProfesionalYaExisteException extends RuntimeException {
    public PerfilProfesionalYaExisteException() {
        super("El prestador ya tiene un perfil profesional; edítalo en lugar de crear otro.");
    }
}
