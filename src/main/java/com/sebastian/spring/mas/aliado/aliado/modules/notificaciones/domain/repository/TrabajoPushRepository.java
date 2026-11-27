package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository;

import java.time.Duration;
import java.util.UUID;

/**
 * Puerto de persistencia del trabajo push asociado a un aviso (F09-PUSH-05).
 *
 * <p>El encolado debe ejecutarse dentro de la transacción de negocio que registra el
 * aviso. La implementación usa la unicidad por aviso y canal de V9 y emite
 * {@code pg_notify} en esa misma transacción, de modo que PostgreSQL solo despierta al
 * worker cuando el trabajo queda confirmado.</p>
 */
public interface TrabajoPushRepository {

    /**
     * Crea el trabajo {@code PENDIENTE_PREPARACION} del aviso si todavía no existe y
     * emite la señal de trabajo en la transacción actual.
     *
     * @return {@code true} solo si esta llamada creó el trabajo; un reproceso no
     *         duplica la fila.
     */
    boolean encolarSiNoExiste(UUID notificacionId, Duration vigencia);
}
