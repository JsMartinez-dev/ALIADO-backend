package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.InstalacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.VinculoPush;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de instalaciones y vínculos push Android (F09-PUSH-01/D-08).
 *
 * <p>Las operaciones de escritura de registro se ejecutan dentro de una transacción
 * proporcionada por la capa de configuración. {@link #bloquearRegistro()} serializa a los
 * escritores de dispositivos para que la resolución de alta, rotación y cambio de cuenta
 * sea atómica; {@link #bloquearInstalacion(UUID)} bloquea además la fila de la instalación
 * mientras dura la transacción.</p>
 */
public interface DispositivoPushRepository {

    /** Serializa las escrituras de registro de dispositivos dentro de la transacción actual. */
    void bloquearRegistro();

    /** Crea la instalación si no existe; conserva la huella original si ya estaba registrada. */
    void asegurarInstalacion(InstalacionPush instalacion);

    /** Busca una instalación sin bloquearla. */
    Optional<InstalacionPush> buscarInstalacion(UUID instalacionId);

    /** Busca y bloquea la fila de la instalación para el resto de la transacción. */
    Optional<InstalacionPush> bloquearInstalacion(UUID instalacionId);

    /** Vínculo vigente de la instalación, si existe. */
    Optional<VinculoPush> vinculoActivo(UUID instalacionId);

    /** Indica si la huella de credencial ya fue usada por un vínculo activo o retirado. */
    boolean existeCredencial(byte[] huellaCredencial);

    /** Instalación que tiene activo ese token FCM, si existe. */
    Optional<UUID> instalacionConTokenActivo(byte[] huellaToken);

    /** Siguiente generación disponible para la instalación. */
    long siguienteGeneracion(UUID instalacionId);

    /** Persiste un vínculo nuevo y activo. */
    void guardarVinculo(VinculoPush vinculo);

    /** Revoca la generación indicada si sigue activa. */
    boolean revocarVinculo(UUID instalacionId, long generacion, OffsetDateTime momento);

    /** Revoca el vínculo activo propio del usuario autenticado si la credencial coincide. */
    boolean revocarVinculoPropio(UUID instalacionId, UUID usuarioId, byte[] huellaCredencial,
                                 OffsetDateTime momento);

    /** Revoca la generación activa identificada por la credencial, sin sesión. */
    boolean revocarVinculoPorCredencial(UUID instalacionId, byte[] huellaCredencial,
                                        OffsetDateTime momento);
}
