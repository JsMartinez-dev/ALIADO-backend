package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception;
public class PortafolioInvalidoException extends RuntimeException {
    public PortafolioInvalidoException() { super("La descripción es obligatoria."); }
}
