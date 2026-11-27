package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler;

import java.util.function.Consumer;

/**
 * Sesión dedicada de escucha PostgreSQL ({@code LISTEN aliado_notificaciones_trabajo}).
 *
 * <p>Debe ser una conexión propia, fuera del pool de consultas de la aplicación, y
 * permanecer en autocommit para que la suscripción se confirme al ejecutar
 * {@code LISTEN}. La escucha usa la espera bloqueante del driver: no hay sondeo.</p>
 *
 * <p>La interfaz existe para poder probar el coordinador con un doble sin PostgreSQL.
 * La implementación real es {@link EscuchaTrabajoPushPostgres}.</p>
 */
public interface EscuchaTrabajoPush extends AutoCloseable {

    /**
     * Conecta y confirma la suscripción. Al volver sin excepción, {@code LISTEN} ya está
     * confirmado en la sesión y la cola persistida puede leerse sin perder señales.
     */
    void abrir() throws Exception;

    /**
     * Espera señales de forma bloqueante. Cada señal confirmada invoca
     * {@code alRecibirSenal}; si la sesión se pierde, marca la escucha inactiva e invoca
     * {@code alPerderConexion}. Solo vuelve al cerrarse la sesión.
     */
    void escuchar(Runnable alRecibirSenal, Consumer<Exception> alPerderConexion);

    boolean estaActiva();

    @Override
    void close();
}
