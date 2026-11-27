package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application;

import com.sebastian.spring.mas.aliado.aliado.common.events.PublicadorEventos;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.AvisoBandejaRegistrado;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.NotificacionConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.NotificacionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ConsultaPreferenciasPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Canal;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntrega;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Notificaciones (SPEC-09, UC057–UC058; SPEC-06/07 como productores).
 * Registro dentro de la transacción de negocio; la entrega del canal de bandeja se
 * dispara por el evento del aviso registrado, después del commit, sin sondeo periódico de
 * la tabla (F09-PUSH-11). Sin endpoint de creación: una notificación nunca se genera sin
 * un evento real (SC-004).
 */
@Service
public class NotificacionService implements NotificacionPort, NotificacionConsultaPort {
    private final NotificacionRepository notificaciones;
    private final ConsultaPreferenciasPort preferencias;
    private final PublicadorEventos eventos;

    public NotificacionService(NotificacionRepository notificaciones,
                               ConsultaPreferenciasPort preferencias,
                               PublicadorEventos eventos) {
        this.notificaciones = notificaciones;
        this.preferencias = preferencias;
        this.eventos = eventos;
    }

    @Override
    @Transactional
    public void registrar(TipoNotificacion tipo, UUID aggregateId, UUID destinatarioId, int eventVersion,
                          String detalle, Canal canalEntrega) {
        if (notificaciones.existeEvento(tipo.name(), aggregateId, destinatarioId, eventVersion)) {
            return; // Idempotencia por evento (plan general 19.3): no duplicar avisos.
        }
        Canal canalEfectivo = canalEntrega == null ? Canal.PUSH : canalEntrega;
        // SPEC-14 (F14-T004): no registrar avisos de un tipo/canal que el usuario desactivó.
        if (!preferencias.avisoHabilitado(destinatarioId, tipo.name(), canalEfectivo.name())) {
            return;
        }
        Notificacion notificacion = new Notificacion(UUID.randomUUID(), destinatarioId, tipo, aggregateId,
                eventVersion, detalle, canalEfectivo, EstadoEntrega.PENDIENTE, 0, OffsetDateTime.now(), null, null);
        notificaciones.guardar(notificacion);
        // Solo este aviso se entrega, y solo tras confirmar la transacción de negocio.
        eventos.publicar(new AvisoBandejaRegistrado(notificacion.id()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionConsulta> listarPropias(UUID destinatarioId) {
        return notificaciones.porDestinatario(destinatarioId).stream().map(this::aConsulta).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionConsulta> listarPorTipo(UUID destinatarioId, TipoNotificacion tipo) {
        return notificaciones.porDestinatarioYTipo(destinatarioId, tipo.name()).stream().map(this::aConsulta).toList();
    }

    private NotificacionConsulta aConsulta(Notificacion n) {
        // La interpretación del agregado vive en el dominio y la comparte la bandeja paginada.
        return new NotificacionConsulta(n.id(), n.tipo(), n.servicioId(), n.solicitudId(),
                n.detalle(), n.estadoEntrega(), n.fechaGeneracion());
    }
}
