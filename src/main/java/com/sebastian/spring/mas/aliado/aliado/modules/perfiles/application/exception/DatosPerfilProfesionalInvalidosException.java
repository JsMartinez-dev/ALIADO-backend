package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception;
public class DatosPerfilProfesionalInvalidosException extends RuntimeException {
 private final String codigo;
 public DatosPerfilProfesionalInvalidosException(String codigo, String mensaje) {super(mensaje);this.codigo=codigo;}
 public String codigo() {return codigo;}
}
