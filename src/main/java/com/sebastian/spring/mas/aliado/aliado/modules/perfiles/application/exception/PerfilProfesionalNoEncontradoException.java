package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception;
public class PerfilProfesionalNoEncontradoException extends RuntimeException {
    public PerfilProfesionalNoEncontradoException() { super("Primero debes crear tu perfil profesional."); }
}
