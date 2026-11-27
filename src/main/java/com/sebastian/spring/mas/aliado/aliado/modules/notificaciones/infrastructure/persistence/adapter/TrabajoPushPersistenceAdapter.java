package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.persistence.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.TrabajoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.SenalTrabajoPush;
import java.time.Duration;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Adaptador PostgreSQL del trabajo push (V9, F09-PUSH-05).
 *
 * <p>{@code JdbcTemplate} participa en la transacción abierta por la capa de
 * configuración de publicación: si esa transacción revierte, la fila y la señal
 * {@code pg_notify} desaparecen juntas. La señal se emite solo cuando esta llamada crea
 * el trabajo; el payload vacío permite agrupar señales y no expone datos del aviso.</p>
 */
@Component
public class TrabajoPushPersistenceAdapter implements TrabajoPushRepository {
    private static final String SQL_ENCOLAR = """
            INSERT INTO trabajos_push (id, notificacion_id, canal, estado, expira_en)
            VALUES (?, ?, 'PUSH', 'PENDIENTE_PREPARACION', CURRENT_TIMESTAMP + make_interval(mins => ?::int))
            ON CONFLICT (notificacion_id, canal) DO NOTHING
            """;

    private final JdbcTemplate jdbc;

    public TrabajoPushPersistenceAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean encolarSiNoExiste(UUID notificacionId, Duration vigencia) {
        long minutos = Math.max(1L, vigencia.toMinutes());
        int creados = jdbc.update(SQL_ENCOLAR, UUID.randomUUID(), notificacionId, minutos);
        if (creados == 1) {
            jdbc.execute("SELECT pg_notify('" + SenalTrabajoPush.CANAL + "', '')");
        }
        return creados == 1;
    }
}
