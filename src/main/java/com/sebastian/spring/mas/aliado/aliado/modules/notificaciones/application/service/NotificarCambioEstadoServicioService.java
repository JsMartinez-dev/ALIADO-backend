package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.events.Eventos;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.NotificacionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.NotificarCambioEstadoServicioUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.*;
import java.util.UUID;

public class NotificarCambioEstadoServicioService implements NotificarCambioEstadoServicioUseCase {
    private final NotificacionPort notificaciones;
    public NotificarCambioEstadoServicioService(NotificacionPort notificaciones) { this.notificaciones = notificaciones; }
    @Override public void ejecutar(Eventos.ServicioEstadoCambiado evento) {
        UUID destinatario = evento.actorId().equals(evento.clienteId()) ? evento.prestadorId() : evento.clienteId();
        notificaciones.registrar(TipoNotificacion.CAMBIO_ESTADO_SERVICIO, evento.servicioId(), destinatario, evento.version(), "El servicio cambió de estado a " + evento.estadoNuevo() + ".", Canal.PUSH);
    }
}
