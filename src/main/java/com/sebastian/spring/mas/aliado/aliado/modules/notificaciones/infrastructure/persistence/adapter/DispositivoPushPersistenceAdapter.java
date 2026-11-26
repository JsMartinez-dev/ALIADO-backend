package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.persistence.adapter;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.InstalacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.VinculoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.DispositivoPushRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Adaptador PostgreSQL de instalaciones y vínculos push (V9, F09-PUSH-01/D-08).
 *
 * <p>Todo el acceso usa la conexión de la transacción abierta por la capa de configuración.
 * {@code pg_advisory_xact_lock} serializa a los escritores de registro y
 * {@code SELECT ... FOR UPDATE} bloquea la fila de la instalación, de modo que la
 * comprobación y la escritura de la generación vigente no compiten entre sí.</p>
 */
@Component
public class DispositivoPushPersistenceAdapter implements DispositivoPushRepository {
    /** Clave fija de bloqueo consultivo para el registro de dispositivos push. */
    private static final long CLAVE_BLOQUEO_REGISTRO = 918_273_645L;

    private static final String SQL_INSTALACION =
            "SELECT id, plataforma, prueba_sha256, creada_en FROM instalaciones_push WHERE id = ?";
    private static final String SQL_VINCULO_ACTIVO =
            "SELECT instalacion_id, generacion, usuario_id, token_fcm, token_fcm_sha256, "
                    + "revocacion_sha256, vinculado_en, revocado_en "
                    + "FROM vinculos_push WHERE instalacion_id = ? AND revocado_en IS NULL";
    private static final String SQL_REVOCAR =
            "UPDATE vinculos_push SET revocado_en = ?, token_fcm = NULL, token_fcm_sha256 = NULL "
                    + "WHERE instalacion_id = ? AND generacion = ? AND revocado_en IS NULL";

    private final JdbcTemplate jdbc;

    public DispositivoPushPersistenceAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void bloquearRegistro() {
        // La clave es una constante del módulo: no se interpola ningún dato de la petición.
        jdbc.execute("SELECT pg_advisory_xact_lock(" + CLAVE_BLOQUEO_REGISTRO + ")");
    }

    @Override
    public void asegurarInstalacion(InstalacionPush instalacion) {
        jdbc.update("""
                INSERT INTO instalaciones_push (id, plataforma, prueba_sha256)
                VALUES (?, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """, instalacion.id(), instalacion.plataforma(), instalacion.pruebaSha256());
    }

    @Override
    public Optional<InstalacionPush> buscarInstalacion(UUID instalacionId) {
        List<InstalacionPush> filas = jdbc.query(SQL_INSTALACION, this::mapearInstalacion, instalacionId);
        return filas.stream().findFirst();
    }

    @Override
    public Optional<InstalacionPush> bloquearInstalacion(UUID instalacionId) {
        List<InstalacionPush> filas = jdbc.query(SQL_INSTALACION + " FOR UPDATE",
                this::mapearInstalacion, instalacionId);
        return filas.stream().findFirst();
    }

    @Override
    public Optional<VinculoPush> vinculoActivo(UUID instalacionId) {
        List<VinculoPush> filas = jdbc.query(SQL_VINCULO_ACTIVO, this::mapearVinculo, instalacionId);
        return filas.stream().findFirst();
    }

    @Override
    public boolean existeCredencial(byte[] huellaCredencial) {
        Boolean existe = jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM vinculos_push WHERE revocacion_sha256 = ?)",
                Boolean.class, huellaCredencial);
        return Boolean.TRUE.equals(existe);
    }

    @Override
    public Optional<UUID> instalacionConTokenActivo(byte[] huellaToken) {
        List<UUID> filas = jdbc.query(
                "SELECT instalacion_id FROM vinculos_push "
                        + "WHERE token_fcm_sha256 = ? AND revocado_en IS NULL",
                (rs, rowNum) -> rs.getObject("instalacion_id", UUID.class), huellaToken);
        return filas.stream().findFirst();
    }

    @Override
    public long siguienteGeneracion(UUID instalacionId) {
        Long generacion = jdbc.queryForObject(
                "SELECT COALESCE(MAX(generacion), 0) + 1 FROM vinculos_push WHERE instalacion_id = ?",
                Long.class, instalacionId);
        return generacion == null ? 1L : generacion;
    }

    @Override
    public void guardarVinculo(VinculoPush vinculo) {
        try {
            jdbc.update("""
                    INSERT INTO vinculos_push (instalacion_id, generacion, usuario_id, token_fcm,
                                               token_fcm_sha256, revocacion_sha256, vinculado_en, revocado_en)
                    VALUES (?, ?, ?, ?, ?, ?, ?, NULL)
                    """, vinculo.instalacionId(), vinculo.generacion(), vinculo.usuarioId(),
                    vinculo.tokenFcm(), vinculo.tokenFcmSha256(), vinculo.revocacionSha256(),
                    vinculo.vinculadoEn());
        } catch (DuplicateKeyException ex) {
            throw conflictoDeUnicidad(ex);
        }
    }

    @Override
    public boolean revocarVinculo(UUID instalacionId, long generacion, OffsetDateTime momento) {
        return jdbc.update(SQL_REVOCAR, momento, instalacionId, generacion) > 0;
    }

    @Override
    public boolean revocarVinculoPropio(UUID instalacionId, UUID usuarioId, byte[] huellaCredencial,
                                        OffsetDateTime momento) {
        return jdbc.update("""
                UPDATE vinculos_push SET revocado_en = ?, token_fcm = NULL, token_fcm_sha256 = NULL
                WHERE instalacion_id = ? AND usuario_id = ? AND revocacion_sha256 = ?
                  AND revocado_en IS NULL
                """, momento, instalacionId, usuarioId, huellaCredencial) > 0;
    }

    @Override
    public boolean revocarVinculoPorCredencial(UUID instalacionId, byte[] huellaCredencial,
                                               OffsetDateTime momento) {
        return jdbc.update("""
                UPDATE vinculos_push SET revocado_en = ?, token_fcm = NULL, token_fcm_sha256 = NULL
                WHERE instalacion_id = ? AND revocacion_sha256 = ? AND revocado_en IS NULL
                """, momento, instalacionId, huellaCredencial) > 0;
    }

    private InstalacionPush mapearInstalacion(ResultSet rs, int fila) throws SQLException {
        return new InstalacionPush(rs.getObject("id", UUID.class), rs.getString("plataforma"),
                rs.getBytes("prueba_sha256"), rs.getObject("creada_en", OffsetDateTime.class));
    }

    private VinculoPush mapearVinculo(ResultSet rs, int fila) throws SQLException {
        return new VinculoPush(rs.getObject("instalacion_id", UUID.class), rs.getLong("generacion"),
                rs.getObject("usuario_id", UUID.class), rs.getString("token_fcm"),
                rs.getBytes("token_fcm_sha256"), rs.getBytes("revocacion_sha256"),
                rs.getObject("vinculado_en", OffsetDateTime.class),
                rs.getObject("revocado_en", OffsetDateTime.class));
    }

    private RuntimeException conflictoDeUnicidad(DuplicateKeyException ex) {
        // Último recurso ante una carrera entre instalaciones: las comprobaciones previas
        // cubren el caso normal. El nombre de la restricción viene del error de PostgreSQL.
        String detalle = ex.getMostSpecificCause().getMessage();
        if (detalle != null && detalle.contains("uq_vinculo_push_token_activo")) {
            return new BusinessException.Conflict("TOKEN_PUSH_EN_USO",
                    "El token push ya está asociado a otra instalación.");
        }
        return new BusinessException.Conflict("VINCULO_PUSH_EN_USO",
                "El vínculo push no coincide con la credencial vigente.");
    }
}
