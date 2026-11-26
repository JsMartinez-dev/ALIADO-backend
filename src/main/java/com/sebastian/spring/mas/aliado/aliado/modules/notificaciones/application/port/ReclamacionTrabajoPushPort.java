package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResultadoLotePush;
import java.time.Instant;
import java.util.Optional;

/**
 * Puerto de reclamación y drenaje del worker push (F09-PUSH-06/07).
 *
 * <p>Define lo que el worker necesita: un lote elegible acotado y el menor plazo
 * persistido que puede cambiar la elegibilidad. F09-PUSH-07 aporta la implementación SQL
 * con {@code FOR UPDATE SKIP LOCKED}, reclamación con identificador y vencimiento,
 * {@code NOTIFY} transaccional y escrituras de resultado condicionadas. El despacho real
 * por dispositivo llega en F09-PUSH-08 a través de
 * {@link ProcesadorTrabajoPushPort}.</p>
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
