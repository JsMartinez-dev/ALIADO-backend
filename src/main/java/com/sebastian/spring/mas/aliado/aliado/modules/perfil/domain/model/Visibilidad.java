package com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model;


public enum Visibilidad {
    PUBLICO, SOLO_ZONA, PRIVADO;

    public boolean permiteConsultaPublica() {
        return this != PRIVADO;
    }
}
