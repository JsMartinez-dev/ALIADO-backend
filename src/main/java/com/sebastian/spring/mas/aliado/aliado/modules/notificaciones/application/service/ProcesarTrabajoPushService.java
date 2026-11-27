package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.IteracionTrabajoPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResultadoIteracionTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResultadoLotePush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.ReclamacionTrabajoPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ProcesarTrabajoPushUseCase;

/**
 * Ciclo del worker push (F09-PUSH-06).
 *
 * <p>Procesa como máximo un lote acotado por iteración. Si el lote quedó lleno, informa
 * que resta trabajo para que el coordinador drene de inmediato sin consultar plazos;
 * si se agotó, devuelve el menor plazo persistido para armar el único temporizador. Con
 * cola vacía y sin plazo el resultado es {@link ResultadoIteracionTrabajoPush#enReposo()}
 * y el worker espera bloqueado en {@code LISTEN}.</p>
 */
public class ProcesarTrabajoPushService implements ProcesarTrabajoPushUseCase {
    private final ReclamacionTrabajoPushPort reclamacion;

    public ProcesarTrabajoPushService(ReclamacionTrabajoPushPort reclamacion) {
        this.reclamacion = reclamacion;
    }

    @Override
    public ResultadoIteracionTrabajoPush ejecutar(IteracionTrabajoPushCommand command) {
        ResultadoLotePush lote = reclamacion.procesarLoteElegible();
        if (lote.quedanElegibles()) {
            return ResultadoIteracionTrabajoPush.drenarDeInmediato();
        }
        return reclamacion.proximoPlazo()
                .map(ResultadoIteracionTrabajoPush::esperarHasta)
                .orElseGet(ResultadoIteracionTrabajoPush::enReposo);
    }
}
