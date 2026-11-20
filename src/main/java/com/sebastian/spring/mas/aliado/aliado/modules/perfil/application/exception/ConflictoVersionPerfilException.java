package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception;

public class ConflictoVersionPerfilException extends RuntimeException {
    public ConflictoVersionPerfilException() {
        super("El perfil cambió desde tu última lectura; vuelve a cargarlo e intenta la modificación de nuevo.");
    }
}
