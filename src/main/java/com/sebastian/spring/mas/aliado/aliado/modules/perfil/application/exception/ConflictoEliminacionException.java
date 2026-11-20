package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception;

public class ConflictoEliminacionException extends RuntimeException {
    public ConflictoEliminacionException() {
        super("La solicitud de eliminación cambió. Vuelve a consultar su estado e inténtalo de nuevo.");
    }
}
