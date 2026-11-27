package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

/**
 * Resultado acotado de reclamar y drenar un lote del worker push.
 *
 * <p>Lo produce el puerto de reclamación. {@code quedanElegibles} indica que el
 * coordinador debe ejecutar otra iteración antes de volver a esperar; no autoriza a
 * consultar la tabla en reposo.</p>
 *
 * @param procesados      elementos reclamados y procesados en este lote
 * @param quedanElegibles si el lote quedó lleno y aún hay trabajo elegible
 */
public record ResultadoLotePush(int procesados, boolean quedanElegibles) {
    public ResultadoLotePush {
        if (procesados < 0) {
            throw new IllegalArgumentException("procesados no puede ser negativo");
        }
    }
}
