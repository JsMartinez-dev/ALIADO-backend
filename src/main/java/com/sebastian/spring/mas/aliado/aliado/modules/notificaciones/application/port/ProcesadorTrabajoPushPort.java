package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;

/**
 * Procesamiento de una reclamación del worker push, fuera de la transacción de
 * reclamación (F09-PUSH-07).
 *
 * <p>F09-PUSH-08 implementa la preparación idempotente de entregas y el envío por
 * dispositivo. Este puerto permite que el adaptador de reclamación entregue cada fila ya
 * reclamada sin abrir una transacción larga alrededor de una llamada externa; los
 * resultados se persisten con {@code ReclamacionTrabajoPushRepository} condicionados al
 * identificador de reclamación y su vencimiento.</p>
 */
public interface ProcesadorTrabajoPushPort {

    /**
     * Prepara o envía una reclamación. Un fallo deja la reclamación persistida: otra
     * instancia (o la misma) la recuperará al vencer su plazo.
     */
    void procesar(ReclamacionPush reclamacion);
}
