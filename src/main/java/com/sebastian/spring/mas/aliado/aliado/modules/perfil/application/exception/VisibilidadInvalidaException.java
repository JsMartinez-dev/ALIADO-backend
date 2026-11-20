package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception;

public class VisibilidadInvalidaException extends RuntimeException {
    public VisibilidadInvalidaException() {
        super("Preferencia de visibilidad inválida. Valores: PUBLICO, SOLO_ZONA, PRIVADO.");
    }
}
