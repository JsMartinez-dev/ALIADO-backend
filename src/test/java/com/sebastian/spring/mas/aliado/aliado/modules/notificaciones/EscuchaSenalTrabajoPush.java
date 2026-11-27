package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.SenalTrabajoPush;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

/**
 * Conexión dedicada fuera del pool para las pruebas PostgreSQL de F09-PUSH-05:
 * suscribe {@code LISTEN} antes de publicar y espera de forma bloqueante la señal.
 */
final class EscuchaSenalTrabajoPush implements AutoCloseable {
    private final Connection conexion;
    private final PGConnection pg;

    private EscuchaSenalTrabajoPush(Connection conexion, PGConnection pg) {
        this.conexion = conexion;
        this.pg = pg;
    }

    static EscuchaSenalTrabajoPush abrir() throws SQLException {
        Connection conexion = DriverManager.getConnection(System.getenv("ALIADO_PG_URL"),
                leer("ALIADO_PG_USERNAME", "aliado"), leer("ALIADO_PG_PASSWORD", "aliado"));
        conexion.setAutoCommit(true);
        try (Statement statement = conexion.createStatement()) {
            statement.execute("LISTEN " + SenalTrabajoPush.CANAL);
        }
        PGConnection pg = conexion.unwrap(PGConnection.class);
        // getNotifications(0) bloquea indefinidamente en el driver: sin drenaje previo basta
        // con que la suscripción esté activa antes de publicar.
        return new EscuchaSenalTrabajoPush(conexion, pg);
    }

    PGNotification[] esperar(int milisegundos) throws SQLException {
        long limite = System.currentTimeMillis() + milisegundos;
        while (System.currentTimeMillis() < limite) {
            PGNotification[] senales = pg.getNotifications(250);
            if (senales != null && senales.length > 0) {
                return senales;
            }
        }
        return new PGNotification[0];
    }

    @Override
    public void close() throws SQLException {
        conexion.close();
    }

    private static String leer(String variable, String porDefecto) {
        String valor = System.getenv(variable);
        return valor == null || valor.isBlank() ? porDefecto : valor;
    }
}
