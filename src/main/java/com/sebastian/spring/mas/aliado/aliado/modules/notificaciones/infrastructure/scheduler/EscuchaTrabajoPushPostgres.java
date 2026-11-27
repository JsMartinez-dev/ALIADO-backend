package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Escucha PostgreSQL real sobre una conexión JDBC dedicada (F09-PUSH-06).
 *
 * <p>La conexión se abre con {@link DriverManager}, no con el {@code DataSource} de la
 * aplicación, porque un pool puede devolver la sesión a otro consumidor. La suscripción
 * se confirma ejecutando {@code LISTEN} en autocommit y comprobando el canal con
 * {@code pg_listening_channels()} antes de exponer la escucha como activa. La espera usa
 * {@code PGConnection.getNotifications(0)}, que bloquea en el driver sin sondeo. TCP
 * keepalive queda activado para detectar sesiones rotas en el transporte.</p>
 *
 * <p>Para el despliegue, la URL debe apuntar a un endpoint que conserve la sesión; con
 * un pooler en modo transacción hace falta una conexión directa o un pool de sesión.
 * Validación pendiente en Azure (F09-PUSH-02/09).</p>
 */
public final class EscuchaTrabajoPushPostgres implements EscuchaTrabajoPush {
    private static final Logger log = LoggerFactory.getLogger(EscuchaTrabajoPushPostgres.class);
    private static final Pattern IDENTIFICADOR = Pattern.compile("[a-z_][a-z0-9_]{0,62}");

    private final String url;
    private final String usuario;
    private final String contrasena;
    private final String canal;

    private Connection conexion;
    private PGConnection conexionPg;
    private volatile boolean activa;

    public EscuchaTrabajoPushPostgres(String url, String usuario, String contrasena, String canal) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("La URL de la escucha push es obligatoria");
        }
        if (canal == null || !IDENTIFICADOR.matcher(canal).matches()) {
            throw new IllegalArgumentException("Canal de escucha inválido: " + canal);
        }
        this.url = url;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.canal = canal;
    }

    @Override
    public synchronized void abrir() throws SQLException {
        cerrarConexion();
        Properties propiedades = new Properties();
        if (usuario != null && !usuario.isBlank()) {
            propiedades.setProperty("user", usuario);
        }
        if (contrasena != null) {
            propiedades.setProperty("password", contrasena);
        }
        propiedades.setProperty("tcpKeepAlive", "true");
        propiedades.setProperty("ApplicationName", "aliado-push-listen");

        Connection nueva = DriverManager.getConnection(url, propiedades);
        try {
            nueva.setAutoCommit(true);
            try (Statement sentencia = nueva.createStatement()) {
                sentencia.execute("LISTEN " + canal);
            }
            // LISTEN toma efecto al confirmar; en autocommit la sentencia ya está
            // confirmada. Esta comprobación explícita es la confirmación de la suscripción.
            try (PreparedStatement comprobacion = nueva.prepareStatement(
                    "SELECT COUNT(*) FROM pg_listening_channels() AS escucha WHERE escucha = ?")) {
                comprobacion.setString(1, canal);
                try (ResultSet filas = comprobacion.executeQuery()) {
                    filas.next();
                    if (filas.getInt(1) != 1) {
                        throw new SQLException("LISTEN no confirmado para el canal " + canal);
                    }
                }
            }
            this.conexion = nueva;
            this.conexionPg = nueva.unwrap(PGConnection.class);
            this.activa = true;
            log.info("Escucha push suscrita al canal {} sobre una conexión dedicada", canal);
        } catch (SQLException | RuntimeException ex) {
            try {
                nueva.close();
            } catch (SQLException cierreIgnorado) {
                log.debug("No se pudo cerrar la conexión de escucha fallida", cierreIgnorado);
            }
            throw ex;
        }
    }

    @Override
    public void escuchar(Runnable alRecibirSenal, Consumer<Exception> alPerderConexion) {
        PGConnection escucha = this.conexionPg;
        try {
            while (activa && escucha != null) {
                // 0 = espera indefinida en el driver; el cierre de la conexión la desbloquea.
                PGNotification[] notificaciones = escucha.getNotifications(0);
                if (notificaciones != null && notificaciones.length > 0) {
                    alRecibirSenal.run();
                }
            }
        } catch (SQLException | RuntimeException ex) {
            boolean estabaActiva = activa;
            activa = false;
            if (estabaActiva) {
                alPerderConexion.accept(ex);
            }
        }
    }

    @Override
    public boolean estaActiva() {
        return activa;
    }

    @Override
    public synchronized void close() {
        activa = false;
        cerrarConexion();
    }

    private void cerrarConexion() {
        Connection actual = conexion;
        conexion = null;
        conexionPg = null;
        if (actual != null) {
            try {
                actual.close();
            } catch (SQLException cierreIgnorado) {
                log.debug("No se pudo cerrar la conexión de escucha", cierreIgnorado);
            }
        }
    }
}
