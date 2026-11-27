package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Resultado del envío a un dispositivo concreto (F09-PUSH-07/F09-PUSH-08).
 *
 * <p>Se persiste solo si la reclamación indicada sigue vigente y la generación del
 * resultado coincide con la generación de la entrega. Un resultado tardío de una
 * reclamación vencida, reemplazada o de una generación antigua no modifica la fila.</p>
 *
 * <p>Las invariantes replican las restricciones de V9: un reintento
 * ({@link EstadoEntregaPush#PENDIENTE}) exige {@code proximoIntentoEn}; un estado
 * terminal lo prohíbe; solo {@link EstadoEntregaPush#ACEPTADA} lleva identificador de
 * mensaje de FCM.</p>
 */
public record ResultadoEntregaPush(UUID entregaId, UUID reclamacionId, long generacion,
                                   EstadoEntregaPush estado, int intentos,
                                   Instant proximoIntentoEn, String idMensajeFcm,
                                   String ultimoCodigoError) {

    public ResultadoEntregaPush {
        Objects.requireNonNull(entregaId, "La entrega del resultado es obligatoria");
        Objects.requireNonNull(reclamacionId, "La reclamación del resultado es obligatoria");
        Objects.requireNonNull(estado, "El estado del resultado es obligatorio");
        if (generacion < 1) {
            throw new IllegalArgumentException("La generación del resultado debe ser positiva");
        }
        if (intentos < 0) {
            throw new IllegalArgumentException("Los intentos del resultado no pueden ser negativos");
        }
        if (estado == EstadoEntregaPush.PENDIENTE) {
            if (proximoIntentoEn == null) {
                throw new IllegalArgumentException("Un reintento exige próximo intento");
            }
        } else if (proximoIntentoEn != null) {
            throw new IllegalArgumentException("Un estado terminal no admite próximo intento");
        }
        if (estado == EstadoEntregaPush.ACEPTADA) {
            if (idMensajeFcm == null || idMensajeFcm.isBlank()) {
                throw new IllegalArgumentException("Una entrega aceptada exige identificador de FCM");
            }
        } else if (idMensajeFcm != null) {
            throw new IllegalArgumentException("Solo una entrega aceptada lleva identificador de FCM");
        }
    }
}
