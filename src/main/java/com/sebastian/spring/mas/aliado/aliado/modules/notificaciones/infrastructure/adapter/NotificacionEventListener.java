package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.common.events.Eventos;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.NotificacionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Canal;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
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
    private final NotificacionPort notificaciones;
    private final NotificarPropuestaRecibidaUseCase notificarPropuesta;
    private final DetectarNuevaOportunidadUseCase detectarNuevaOportunidad;

    public NotificacionEventListener(NotificacionPort notificaciones, DetectarNuevaOportunidadUseCase detectarNuevaOportunidad, NotificarPropuestaRecibidaUseCase notificarPropuesta) {
        this.notificaciones = notificaciones;
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
        notificaciones.registrar(TipoNotificacion.ACEPTACION_PROPUESTA, evento.servicioId(), evento.prestadorId(), 1,
                "Tu propuesta fue aceptada. El servicio está contratado.", Canal.PUSH);
    }

    @EventListener
    public void alCambiarEstadoServicio(Eventos.ServicioEstadoCambiado evento) {
        // La contraparte es quien no ejecutó la transición.
        var destinatario = evento.actorId().equals(evento.clienteId())
                ? evento.prestadorId() : evento.clienteId();
        notificaciones.registrar(TipoNotificacion.CAMBIO_ESTADO_SERVICIO, evento.servicioId(), destinatario,
                evento.version(),
                "El servicio cambió de estado a " + evento.estadoNuevo() + ".", Canal.PUSH);
    }
}
