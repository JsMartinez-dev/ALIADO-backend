package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Estado observable de la cola push en el momento en que el worker termina un lote
 * (F09-PUSH-11).
 *
 * <p>Se lee solo después de procesar trabajo, nunca con la cola en reposo, para que la
 * instrumentación no introduzca lecturas periódicas. {@code masAntiguoEn} es el encolado
 * más antiguo entre los trabajos abiertos; {@code null} cuando la cola está vacía.</p>
 */
public record PendientesPush(int trabajosAbiertos, int entregasPendientes, Instant masAntiguoEn) {

    public PendientesPush {
        if (trabajosAbiertos < 0) {
            throw new IllegalArgumentException("Los trabajos abiertos no pueden ser negativos");
        }
        if (entregasPendientes < 0) {
            throw new IllegalArgumentException("Las entregas pendientes no pueden ser negativas");
        }
        if (trabajosAbiertos == 0 && masAntiguoEn != null) {
            throw new IllegalArgumentException("Sin trabajos abiertos no hay encolado más antiguo");
        }
    }

    public static PendientesPush vacio() {
        return new PendientesPush(0, 0, null);
    }

    /** Antigüedad del trabajo abierto más antiguo; vacío cuando la cola está vacía. */
    public Duration antiguedadEn(Instant ahora) {
        Objects.requireNonNull(ahora, "El instante de referencia es obligatorio");
        return masAntiguoEn == null ? Duration.ZERO : Duration.between(masAntiguoEn, ahora);
    }
}
