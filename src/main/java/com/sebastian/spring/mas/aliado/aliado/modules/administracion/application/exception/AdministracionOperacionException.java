package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.exception;

public class AdministracionOperacionException extends RuntimeException {
  public enum Tipo {
    DATOS_INVALIDOS,
    NO_ENCONTRADO,
    CONFLICTO,
    NO_IMPLEMENTADO
  }

  private final Tipo tipo;
  private final String codigo;

  public AdministracionOperacionException(Tipo tipo, String codigo, String mensaje) {
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
