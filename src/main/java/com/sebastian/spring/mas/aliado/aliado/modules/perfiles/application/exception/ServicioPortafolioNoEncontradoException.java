package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception;
public class ServicioPortafolioNoEncontradoException extends RuntimeException {
    public ServicioPortafolioNoEncontradoException() { super("El servicio de portafolio no existe o no te pertenece."); }
}
