package com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port;

public interface HasheadorPassword {
    String hashear(String password);

    boolean verificar(String password, String passwordHash);
}
