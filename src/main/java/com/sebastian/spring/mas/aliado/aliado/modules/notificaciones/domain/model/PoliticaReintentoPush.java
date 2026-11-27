package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.DoubleSupplier;

/**
 * Política de reintento por dispositivo del piloto push (F09-PUSH-01/D-08).
 *
 * <p>El backoff es exponencial desde {@code retrasoBase} y crece con los intentos ya
 * realizados. El jitter no reduce el mínimo: multiplica el retraso por un factor entre
 * {@code 1.0} y {@code 1.5} según la fuente aleatoria inyectada. El próximo intento es el
 * máximo entre la fecha indicada por {@code Retry-After} y el backoff calculado.</p>
 *
 * <p>La clase es Java puro y no depende del reloj: el instante actual lo aporta el caso
 * de uso para poder probarla con un reloj controlado.</p>
 */
public final class PoliticaReintentoPush {

    private static final int EXPONENTE_MAXIMO = 10;

    private final int intentosMaximos;
    private final Duration retrasoBase;
    private final DoubleSupplier jitter;

    public PoliticaReintentoPush(int intentosMaximos, Duration retrasoBase, DoubleSupplier jitter) {
        if (intentosMaximos < 1) {
            throw new IllegalArgumentException("El máximo de intentos debe ser positivo");
        }
        if (retrasoBase == null || retrasoBase.isZero() || retrasoBase.isNegative()) {
            throw new IllegalArgumentException("El retraso base debe ser positivo");
        }
        this.intentosMaximos = intentosMaximos;
        this.retrasoBase = retrasoBase;
        this.jitter = Objects.requireNonNull(jitter, "La fuente de jitter es obligatoria");
    }

    public int intentosMaximos() {
        return intentosMaximos;
    }

    /** {@code intentosRealizados} incluye el intento que acaba de fallar. */
    public boolean intentosAgotados(int intentosRealizados) {
        return intentosRealizados >= intentosMaximos;
    }

    /**
     * Fecha del siguiente intento: {@code max(Retry-After, ahora + backoff con jitter)}.
     *
     * @param ahora              instante de referencia del caso de uso
     * @param intentosRealizados intentos consumidos, incluido el fallido
     * @param retryAfter         fecha mínima indicada por el proveedor, o {@code null}
     */
    public Instant proximoIntento(Instant ahora, int intentosRealizados, Instant retryAfter) {
        Objects.requireNonNull(ahora, "El instante de referencia es obligatorio");
        Instant backoff = ahora.plus(retraso(intentosRealizados));
        if (retryAfter != null && retryAfter.isAfter(backoff)) {
            return retryAfter;
        }
        return backoff;
    }

    /** Retraso base de la política para los intentos ya realizados. */
    public Duration retraso(int intentosRealizados) {
        int exponente = Math.min(Math.max(intentosRealizados - 1, 0), EXPONENTE_MAXIMO);
        long baseMs = retrasoBase.toMillis() * (1L << exponente);
        double factor = 1.0 + 0.5 * acotar(jitter.getAsDouble());
        return Duration.ofMillis(Math.max(1L, (long) (baseMs * factor)));
    }

    private static double acotar(double valor) {
        if (Double.isNaN(valor) || valor < 0) {
            return 0;
        }
        return Math.min(valor, 1);
    }
}
