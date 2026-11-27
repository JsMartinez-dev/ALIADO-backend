package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler;

import java.time.Duration;
import java.util.function.DoubleSupplier;

/**
 * Retroceso exponencial con jitter para reconectar la sesión de escucha (F09-PUSH-06).
 *
 * <p>El retraso bruto duplica el intento anterior hasta el máximo configurado. El jitter
 * aplica un factor entre 0,5 y 1,0 para dispersar a las instancias. Un cierre correcto de
 * la suscripción reinicia la secuencia.</p>
 */
public final class RetrocesoReconexionPush {
    private static final int TOPE_EXPONENTE = 20;

    private final long baseMs;
    private final long maximoMs;
    private final DoubleSupplier aleatorio;
    private int intento;

    public RetrocesoReconexionPush(Duration base, Duration maximo) {
        this(base, maximo, Math::random);
    }

    public RetrocesoReconexionPush(Duration base, Duration maximo, DoubleSupplier aleatorio) {
        if (base == null || maximo == null || base.isNegative() || base.isZero()
                || maximo.compareTo(base) < 0) {
            throw new IllegalArgumentException("Retroceso inválido: base=" + base + " máximo=" + maximo);
        }
        this.baseMs = base.toMillis();
        this.maximoMs = maximo.toMillis();
        this.aleatorio = aleatorio;
    }

    /** Siguiente espera con retroceso y jitter; avanza el contador de intentos. */
    public Duration siguiente() {
        double bruto = baseMs * Math.pow(2.0, Math.min(intento, TOPE_EXPONENTE));
        long tope = (long) Math.min(bruto, (double) maximoMs);
        intento = Math.min(intento + 1, TOPE_EXPONENTE);
        double factor = 0.5 + Math.min(0.999_999, Math.max(0.0, aleatorio.getAsDouble())) * 0.5;
        return Duration.ofMillis(Math.max(1L, (long) (tope * factor)));
    }

    /** Vuelve al primer intento tras confirmar una suscripción. */
    public void reiniciar() {
        intento = 0;
    }
}
