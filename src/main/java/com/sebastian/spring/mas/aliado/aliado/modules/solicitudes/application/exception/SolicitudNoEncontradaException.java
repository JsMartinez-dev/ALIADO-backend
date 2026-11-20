package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception;

public class SolicitudNoEncontradaException extends RuntimeException {
    public SolicitudNoEncontradaException() { super("Solicitud no encontrada."); }
}
