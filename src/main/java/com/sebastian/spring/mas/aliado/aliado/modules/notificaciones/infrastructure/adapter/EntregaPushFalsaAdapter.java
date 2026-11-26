package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.EntregaPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.CodigosEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.MensajePush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEnvioPush;

/**
 * Doble de entrega push para el piloto local y las pruebas (F09-PUSH-08).
 *
 * <p>Nunca contacta con Firebase ni con la red. Se compone con
 * {@code aliado.push.proveedor=falso} (valor por defecto); el adaptador real de
 * F09-PUSH-09 es {@code EntregaPushFirebaseAdapter}. Clasifica el envío como fallo
 * permanente de configuración para no marcar aceptaciones falsas. Las pruebas de los
 * casos de uso usan sus propios dobles con resultados mixtos.</p>
 */
public class EntregaPushFalsaAdapter implements EntregaPushPort {

    @Override
    public ResultadoEnvioPush enviar(MensajePush mensaje) {
        return ResultadoEnvioPush.permanente(CodigosEntregaPush.PROVEEDOR_NO_CONFIGURADO);
    }
}
