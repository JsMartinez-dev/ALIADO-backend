package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.MensajePush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEnvioPush;

/**
 * Entrega de un mensaje push a un dispositivo concreto (F09-PUSH-08).
 *
 * <p>El adaptador se invoca fuera de la transacción de reclamación: puede tardar y su
 * resultado se persiste después, condicionado a que la reclamación siga vigente y a la
 * generación del token enviada. F09-PUSH-09 aporta la implementación Firebase; el piloto
 * local usa un doble que nunca contacta al proveedor.</p>
 */
public interface EntregaPushPort {

    /**
     * Envía el mensaje y clasifica la respuesta del proveedor.
     *
     * @return aceptación con identificador, error temporal con posible {@code Retry-After},
     *         error permanente o token desregistrado
     */
    ResultadoEnvioPush enviar(MensajePush mensaje);
}
