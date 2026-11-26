package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Trabajo padre push leído por el worker (V9, F09-PUSH-08).
 *
 * <p>No es el agregado del aviso: la bandeja vive en {@link Notificacion} y el trabajo
 * describe solo el ciclo de preparación y entrega por dispositivo. {@code expiraEn} es el
 * plazo compartido por todas las entregas; un próximo intento que lo alcance cierra la
 * entrega como expirada.</p>
 */
public record TrabajoPush(UUID id, UUID notificacionId, EstadoTrabajoPush estado,
                          Instant encoladoEn, Instant expiraEn, String motivoFinal) {

    public TrabajoPush {
        Objects.requireNonNull(id, "El identificador del trabajo es obligatorio");
        Objects.requireNonNull(notificacionId, "El aviso del trabajo es obligatorio");
        Objects.requireNonNull(estado, "El estado del trabajo es obligatorio");
        Objects.requireNonNull(encoladoEn, "El encolado del trabajo es obligatorio");
        Objects.requireNonNull(expiraEn, "La vigencia del trabajo es obligatoria");
        if (!expiraEn.isAfter(encoladoEn)) {
            throw new IllegalArgumentException("La vigencia debe ser posterior al encolado");
        }
        if (estado == EstadoTrabajoPush.FINALIZADO && (motivoFinal == null || motivoFinal.isBlank())) {
            throw new IllegalArgumentException("Un trabajo finalizado conserva su motivo");
        }
        if (estado != EstadoTrabajoPush.FINALIZADO && motivoFinal != null) {
            throw new IllegalArgumentException("Solo un trabajo finalizado conserva motivo");
        }
    }

    public boolean finalizado() {
        return estado == EstadoTrabajoPush.FINALIZADO;
    }

    public boolean preparado() {
        return estado == EstadoTrabajoPush.PREPARADO;
    }

    public boolean expiradoEn(Instant momento) {
        return !momento.isBefore(expiraEn);
    }
}
