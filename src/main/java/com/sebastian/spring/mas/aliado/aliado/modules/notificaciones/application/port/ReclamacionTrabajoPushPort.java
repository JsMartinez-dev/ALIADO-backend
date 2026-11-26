package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResultadoLotePush;
import java.time.Instant;
import java.util.Optional;

/**
 * Puerto de reclamación y drenaje del worker push (F09-PUSH-06).
 *
 * <p>Este puerto define lo que el worker necesita: un lote elegible acotado y el menor
 * plazo persistido que puede cambiar la elegibilidad. La implementación SQL con
 * {@code FOR UPDATE SKIP LOCKED}, reclamación con vencimiento y {@code NOTIFY}
 * transaccional llega en F09-PUSH-07; el despacho real por dispositivo, en F09-PUSH-08.
 * Hasta entonces el ciclo se ejercita con dobles.</p>
 */
public interface ReclamacionTrabajoPushPort {

    /**
     * Reclama y procesa un lote acotado de trabajos de preparación y entregas elegibles.
     * Sin trabajo elegible devuelve {@code procesados = 0} y {@code quedanElegibles = false}.
     */
    ResultadoLotePush procesarLoteElegible();

    /**
     * Menor plazo persistido futuro que puede cambiar la elegibilidad: próximo intento de
     * una entrega, vencimiento de una reclamación o expiración de un trabajo. Vacío si no
     * hay ningún plazo que esperar.
     */
    Optional<Instant> proximoPlazo();
}
