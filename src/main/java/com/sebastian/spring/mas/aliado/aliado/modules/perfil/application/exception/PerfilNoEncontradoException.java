package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception;

/** La cuenta consultada no dispone de un perfil almacenado. */
public class PerfilNoEncontradoException extends RuntimeException {
    public PerfilNoEncontradoException() {
        super("Perfil no encontrado.");
    }
}
