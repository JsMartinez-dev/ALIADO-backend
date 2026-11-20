package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception;

public class ServicioBloqueaEliminacionException extends RuntimeException {
    public ServicioBloqueaEliminacionException() {
        super("Tienes un servicio en ejecución; la eliminación queda bloqueada hasta que finalice.");
    }
}
