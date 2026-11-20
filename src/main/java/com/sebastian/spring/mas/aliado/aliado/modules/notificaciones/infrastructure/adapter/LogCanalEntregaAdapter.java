package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.CanalEntregaNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Canal provisional de registro (P-05). Un log no acredita entrega de NUEVA_OPORTUNIDAD.
 * El proveedor real (FCM/correo) se conecta aquí sin tocar los casos de uso.
 */
@Component
public class LogCanalEntregaAdapter implements CanalEntregaNotificacion {
    private static final Logger log = LoggerFactory.getLogger(LogCanalEntregaAdapter.class);

    @Override
    public boolean disponibleParaEntrega(Notificacion notificacion) {
        // Los avisos auditados esperan un proveedor que acredite entrega.
        return notificacion.tipo() != TipoNotificacion.NUEVA_OPORTUNIDAD
                && notificacion.tipo() != TipoNotificacion.ACEPTACION_PROPUESTA
                && notificacion.tipo() != TipoNotificacion.CAMBIO_ESTADO_SERVICIO;
    }

    @Override
    public boolean entregar(Notificacion notificacion) {
        if (!disponibleParaEntrega(notificacion)) {
            return false;
        }
        log.info("Entrega de notificación {} tipo={} destinatario={} canal={}",
                notificacion.id(), notificacion.tipo(), notificacion.destinatarioId(), notificacion.canal());
        return true;
    }
}
