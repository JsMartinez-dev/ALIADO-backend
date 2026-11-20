package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.exception;

public class FormalizacionOperacionException extends RuntimeException {
  public enum Tipo {
    DATOS_INVALIDOS,
    NO_ENCONTRADO,
    CONFLICTO,
    NO_IMPLEMENTADO
  }

  private final Tipo tipo;
  private final String codigo;

  public FormalizacionOperacionException(Tipo tipo, String codigo, String mensaje) {
    super(mensaje);
    this.tipo = tipo;
    this.codigo = codigo;
  }

  public Tipo tipo() {
    return tipo;
  }

  public String codigo() {
    return codigo;
  }
}
