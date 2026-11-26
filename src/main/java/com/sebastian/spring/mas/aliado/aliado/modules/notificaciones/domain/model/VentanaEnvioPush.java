package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Ventana mínima para iniciar un envío dentro de una reclamación vigente
 * (F09-PUSH-01/D-08, F09-PUSH-08).
 *
 * <p>Antes de invocar al proveedor se exige que el tiempo restante de la reclamación
 * alcance el timeout del proveedor más un margen para registrar el resultado. Si no
 * alcanza, la entrega libera su reclamación sin enviar y vuelve a ser reclamable de
 * inmediato, en lugar de enviar y perder el resultado por un vencimiento inminente.</p>
 *
 * <p>El timeout (5 s en D-08) mide la llamada completa al proveedor desde que se invoca
 * el adaptador; la revalidación ocurre antes y se mide por separado. La clase es Java
 * puro y no conoce el reloj: el instante actual lo aporta el caso de uso.</p>
 */
public final class VentanaEnvioPush {

    private final Duration timeout;
    private final Duration margen;

    public VentanaEnvioPush(Duration timeout, Duration margen) {
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("El timeout de envío debe ser positivo");
        }
        if (margen == null || margen.isNegative()) {
            throw new IllegalArgumentException(
                    "El margen para registrar el resultado no puede ser negativo");
        }
        this.timeout = timeout;
        this.margen = margen;
    }

    /** Tiempo que debe restar de la reclamación para iniciar el envío. */
    public Duration requerido() {
        return timeout.plus(margen);
    }

    /**
     * Indica si el tiempo restante hasta {@code venceEn} alcanza {@link #requerido()}.
     *
     * @param venceEn vencimiento persistido de la reclamación
     * @param ahora   instante actual del worker
     */
    public boolean alcanza(Instant venceEn, Instant ahora) {
        Objects.requireNonNull(venceEn, "El vencimiento de la reclamación es obligatorio");
        Objects.requireNonNull(ahora, "El instante actual es obligatorio");
        return !venceEn.isBefore(ahora.plus(requerido()));
    }
}
