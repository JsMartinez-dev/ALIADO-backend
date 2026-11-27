package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter;

/** Canal PostgreSQL que despierta al worker de trabajos push (payload vacío, sin datos personales). */
public final class SenalTrabajoPush {
    public static final String CANAL = "aliado_notificaciones_trabajo";

    private SenalTrabajoPush() {}
}
