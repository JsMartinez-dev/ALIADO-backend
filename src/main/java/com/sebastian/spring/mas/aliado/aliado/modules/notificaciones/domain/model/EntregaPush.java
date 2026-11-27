package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entrega de un aviso a una instalación concreta (V9, F09-PUSH-08).
 *
 * <p>{@code generacion} identifica el vínculo del token usado al crear la entrega. Antes
 * de enviar se vuelve a comprobar que esa generación siga activa para el destinatario.
 * {@code proximoIntentoEn} solo tiene valor mientras la entrega está
 * {@link EstadoEntregaPush#PENDIENTE}.</p>
 */
public record EntregaPush(UUID id, UUID trabajoId, UUID instalacionId, long generacion,
                          EstadoEntregaPush estado, int intentos, Instant proximoIntentoEn) {

    public EntregaPush {
        Objects.requireNonNull(id, "El identificador de la entrega es obligatorio");
        Objects.requireNonNull(trabajoId, "El trabajo de la entrega es obligatorio");
        Objects.requireNonNull(instalacionId, "La instalación de la entrega es obligatoria");
        Objects.requireNonNull(estado, "El estado de la entrega es obligatorio");
        if (generacion < 1) {
            throw new IllegalArgumentException("La generación de la entrega debe ser positiva");
        }
        if (intentos < 0) {
            throw new IllegalArgumentException("Los intentos de la entrega no pueden ser negativos");
        }
        if (estado == EstadoEntregaPush.PENDIENTE && proximoIntentoEn == null) {
            throw new IllegalArgumentException("Una entrega pendiente conserva su próximo intento");
        }
        if (estado != EstadoEntregaPush.PENDIENTE && proximoIntentoEn != null) {
            throw new IllegalArgumentException("Una entrega terminal no admite próximo intento");
        }
    }

    public boolean pendiente() {
        return estado == EstadoEntregaPush.PENDIENTE;
    }
}
