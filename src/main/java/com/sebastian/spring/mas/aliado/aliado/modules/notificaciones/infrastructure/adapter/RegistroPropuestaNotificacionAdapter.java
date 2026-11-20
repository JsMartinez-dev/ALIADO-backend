package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.port.RegistroNotificacionPropuestaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.NotificacionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.*;
import java.util.UUID;
import org.springframework.stereotype.Component;
@Component
public class RegistroPropuestaNotificacionAdapter implements RegistroNotificacionPropuestaPort {
    private final NotificacionPort notificaciones;
    public RegistroPropuestaNotificacionAdapter(NotificacionPort notificaciones) { this.notificaciones = notificaciones; }
    @Override public void registrar(UUID propuestaId, UUID clienteId) {
        notificaciones.registrar(TipoNotificacion.PROPUESTA_RECIBIDA, propuestaId, clienteId, 1,
                "Recibiste una nueva propuesta para tu solicitud.", Canal.PUSH);
    }
}
