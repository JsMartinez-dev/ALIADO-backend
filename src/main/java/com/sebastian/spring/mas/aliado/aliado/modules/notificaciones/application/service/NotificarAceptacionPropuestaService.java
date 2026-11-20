package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.events.Eventos;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.NotificacionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.NotificarAceptacionPropuestaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.*;

public class NotificarAceptacionPropuestaService implements NotificarAceptacionPropuestaUseCase {
    private final NotificacionPort notificaciones;
    public NotificarAceptacionPropuestaService(NotificacionPort notificaciones) { this.notificaciones = notificaciones; }
    @Override public void ejecutar(Eventos.PropuestaAceptada evento) {
        notificaciones.registrar(TipoNotificacion.ACEPTACION_PROPUESTA, evento.servicioId(), evento.prestadorId(), 1, "Tu propuesta fue aceptada. El servicio está contratado.", Canal.PUSH);
    }
}
