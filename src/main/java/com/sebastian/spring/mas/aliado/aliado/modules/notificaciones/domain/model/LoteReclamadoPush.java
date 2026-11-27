package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Lote reclamado por el worker push (F09-PUSH-07).
 *
 * <p>{@code quedanElegibles} indica que alguna de las dos colas agotó su cupo del lote,
 * de modo que el coordinador debe ejecutar otra iteración antes de volver a esperar. No
 * autoriza a consultar la tabla en reposo: si el lote vuelve vacío el coordinador arma el
 * temporizador con el próximo plazo persistido.</p>
 */
public record LoteReclamadoPush(List<ReclamacionPush> reclamaciones, boolean quedanElegibles) {

    public LoteReclamadoPush {
        Objects.requireNonNull(reclamaciones, "Las reclamaciones del lote son obligatorias");
        reclamaciones = List.copyOf(reclamaciones);
    }

    public static LoteReclamadoPush vacio() {
        return new LoteReclamadoPush(List.of(), false);
    }

    public int procesados() {
        return reclamaciones.size();
    }
}
