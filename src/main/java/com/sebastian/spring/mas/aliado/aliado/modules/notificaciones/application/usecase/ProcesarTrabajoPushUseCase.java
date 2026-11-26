package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.IteracionTrabajoPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResultadoIteracionTrabajoPush;

/**
 * Iteración del worker push (F09-PUSH-06): recibe el motivo del despertar y devuelve si
 * resta trabajo elegible o el próximo plazo que hay que esperar.
 *
 * <p>Una iteración procesa un lote acotado. El coordinador es quien serializa las
 * iteraciones, conserva las señales recibidas durante el drenaje y arma el único
 * temporizador.</p>
 */
public interface ProcesarTrabajoPushUseCase {
    ResultadoIteracionTrabajoPush ejecutar(IteracionTrabajoPushCommand command);
}
