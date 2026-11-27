package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Salida de una iteración del worker push (F09-PUSH-06).
 *
 * <p>Cuando {@code quedanElegibles} es verdadero el coordinador drena otro lote de
 * inmediato y no arma temporizador. Cuando es falso, {@code proximoPlazo} es el menor
 * plazo persistido que puede cambiar la elegibilidad (reintento por dispositivo,
 * vencimiento de una reclamación o expiración de un trabajo). Ausente significa que no
 * hay nada que esperar: el worker queda bloqueado en {@code LISTEN} sin consultar la
 * tabla.</p>
 *
 * @param quedanElegibles si resta trabajo elegible que drenar
 * @param proximoPlazo    próximo plazo persistido, si existe
 */
public record ResultadoIteracionTrabajoPush(boolean quedanElegibles, Optional<Instant> proximoPlazo) {
    public ResultadoIteracionTrabajoPush {
        Objects.requireNonNull(proximoPlazo, "proximoPlazo");
    }

    public static ResultadoIteracionTrabajoPush drenarDeInmediato() {
        return new ResultadoIteracionTrabajoPush(true, Optional.empty());
    }

    public static ResultadoIteracionTrabajoPush enReposo() {
        return new ResultadoIteracionTrabajoPush(false, Optional.empty());
    }

    public static ResultadoIteracionTrabajoPush esperarHasta(Instant plazo) {
        return new ResultadoIteracionTrabajoPush(false, Optional.of(plazo));
    }
}
