package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Resumen operativo del ciclo push (F09-PUSH-11).
 *
 * <p>Agrupa los contadores por tipo y por código corto, más el tamaño y la antigüedad
 * observados de la cola. No contiene tokens, identificadores de instalación, avisos ni
 * contenido del mensaje: solo nombres de tipo, códigos de error y duraciones.</p>
 */
public record ResumenMetricasPush(Map<String, Long> encolados,
                                  Map<String, Long> reclamaciones,
                                  Map<String, Long> reintentos,
                                  Map<String, Long> aceptaciones,
                                  Map<String, Long> fallos,
                                  int trabajosAbiertos,
                                  int entregasPendientes,
                                  Duration latenciaAceptacionMedia,
                                  Duration latenciaAceptacionMaxima,
                                  Duration antiguedadMaxima) {

    public ResumenMetricasPush {
        encolados = ordenar(encolados);
        reclamaciones = ordenar(reclamaciones);
        reintentos = ordenar(reintentos);
        aceptaciones = ordenar(aceptaciones);
        fallos = ordenar(fallos);
        Objects.requireNonNull(latenciaAceptacionMedia, "La latencia media es obligatoria");
        Objects.requireNonNull(latenciaAceptacionMaxima, "La latencia máxima es obligatoria");
        Objects.requireNonNull(antiguedadMaxima, "La antigüedad de la cola es obligatoria");
    }

    /** Línea única apta para el registro operativo, sin datos personales ni secretos. */
    public String describir() {
        return "encolado=" + encolados
                + " reclamacion=" + reclamaciones
                + " reintento=" + reintentos
                + " aceptacion=" + aceptaciones
                + " fallo=" + fallos
                + " pendientes=" + trabajosAbiertos
                + " entregasPendientes=" + entregasPendientes
                + " latenciaMediaMs=" + latenciaAceptacionMedia.toMillis()
                + " latenciaMaximaMs=" + latenciaAceptacionMaxima.toMillis()
                + " antiguedadMaximaMs=" + antiguedadMaxima.toMillis();
    }

    private static Map<String, Long> ordenar(Map<String, Long> valores) {
        Objects.requireNonNull(valores, "Los contadores son obligatorios");
        return Collections.unmodifiableMap(new TreeMap<>(valores));
    }
}
