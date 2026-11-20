package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.common.events.Eventos;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.DetectarNuevaOportunidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.usecase.NotificarPropuestaRecibidaUseCase;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Enganches de notificación (SPEC-09): cada evento de negocio registra su aviso
 * dentro de la misma transacción. La entrega externa la hace el worker, nunca aquí.
 */
@Component
public class NotificacionEventListener {
    private final com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.NotificarAceptacionPropuestaUseCase aceptacion;
    private final com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.NotificarCambioEstadoServicioUseCase cambio;
    private final NotificarPropuestaRecibidaUseCase notificarPropuesta;
    private final DetectarNuevaOportunidadUseCase detectarNuevaOportunidad;

    public NotificacionEventListener(com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.NotificarAceptacionPropuestaUseCase aceptacion, com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.NotificarCambioEstadoServicioUseCase cambio, DetectarNuevaOportunidadUseCase detectarNuevaOportunidad, NotificarPropuestaRecibidaUseCase notificarPropuesta) {
        this.aceptacion = aceptacion;
        this.cambio = cambio;
        this.notificarPropuesta = notificarPropuesta;
        this.detectarNuevaOportunidad = detectarNuevaOportunidad;
    }

    @EventListener
    public void alPublicarSolicitud(Eventos.SolicitudPublicada evento) {
        detectarNuevaOportunidad.ejecutar(evento.solicitudId());
    }

    @EventListener
    public void alCrearPropuesta(Eventos.PropuestaCreada evento) {
        notificarPropuesta.ejecutar(evento.propuestaId());
    }

    @EventListener
    public void alAceptarPropuesta(Eventos.PropuestaAceptada evento) {
        aceptacion.ejecutar(evento);
    }

    @EventListener
    public void alCambiarEstadoServicio(Eventos.ServicioEstadoCambiado evento) {
        cambio.ejecutar(evento);
    }
}
