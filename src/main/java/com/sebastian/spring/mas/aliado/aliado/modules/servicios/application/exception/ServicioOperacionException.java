package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.exception;

public class ServicioOperacionException extends RuntimeException {
 public enum Tipo { INVALIDO, NO_ENCONTRADO, NO_AUTORIZADO, CONFLICTO }
 private final Tipo tipo; private final String codigo;
 public ServicioOperacionException(Tipo tipo,String codigo,String mensaje) {super(mensaje);this.tipo=tipo;this.codigo=codigo;}
 public Tipo tipo(){return tipo;} public String codigo(){return codigo;}
}
