package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

/**
 * Filtro de lectura de la bandeja de avisos (SPEC-09, plan de lectura de bandeja).
 *
 * <p>{@code NO_LEIDA} es el valor por defecto del caso de uso; {@code TODAS} desactiva el
 * filtro. Es un concepto de negocio puro, por lo que vive en el dominio y el puerto de
 * persistencia puede usarlo sin depender de la capa de aplicación.</p>
 */
public enum EstadoLectura {
    NO_LEIDA, LEIDA, TODAS
}
