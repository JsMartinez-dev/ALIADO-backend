package com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model;

/**
 * Valores de preferencia_visibilidad (SPEC-02).
 * Decisión documentada: el SPEC no definía los valores; estos tres cubren la regla base
 * (la zona aproximada es el dato expuesto por defecto, FR-003).
 */
public enum Visibilidad {
    PUBLICO, SOLO_ZONA, PRIVADO;

    /** Ambas opciones visibles exponen solo la zona; nunca autorizan publicar dirección exacta. */
    public boolean permiteConsultaPublica() {
        return this != PRIVADO;
    }
}
