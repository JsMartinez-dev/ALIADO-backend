package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.CanalEntregaNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.EntregaNotificacionesPort;
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
 * Registro dentro de la transacción de negocio; entrega asíncrona posterior con reintentos.
 * Sin endpoint de creación: una notificación nunca se genera sin un evento real (SC-004).
 */
@Service
public class NotificacionService implements NotificacionPort, NotificacionConsultaPort, EntregaNotificacionesPort {
    private final NotificacionRepository notificaciones;
    private final CanalEntregaNotificacion canal;
    private final ConsultaPreferenciasPort preferencias;

    public NotificacionService(NotificacionRepository notificaciones, CanalEntregaNotificacion canal,
                               ConsultaPreferenciasPort preferencias) {
        this.notificaciones = notificaciones;
        this.canal = canal;
        this.preferencias = preferencias;
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
                eventVersion, detalle, canalEfectivo, EstadoEntrega.PENDIENTE, 0, OffsetDateTime.now(), null);
        notificaciones.guardar(notificacion);
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
        UUID servicioId = (n.tipo() == TipoNotificacion.ACEPTACION_PROPUESTA
                || n.tipo() == TipoNotificacion.CAMBIO_ESTADO_SERVICIO) ? n.aggregateId() : null;
        // SPEC-06: el aggregateId de NUEVA_OPORTUNIDAD ES la solicitud que la originó.
        UUID solicitudId = n.tipo() == TipoNotificacion.NUEVA_OPORTUNIDAD ? n.aggregateId() : null;
        return new NotificacionConsulta(n.id(), n.tipo(), servicioId, solicitudId, n.detalle(),
                n.estadoEntrega(), n.fechaGeneracion());
    }

    /** Worker de entrega: reintenta PENDIENTE/FALLIDA; a los 3 intentos queda FALLIDA. */
    @Override
    @Transactional
    public void entregarPendientes() {
        for (Notificacion pendiente : notificaciones.pendientesParaEntrega()) {
            // UC038: sin proveedor real la oportunidad continúa pendiente. No se
            // registra un intento fallido ni se repite un log cada ronda del scheduler.
            if (!canal.disponibleParaEntrega(pendiente)) {
                continue;
            }
            // La preferencia pudo cambiar desde el registro: si ya no está habilitada,
            // se salta la ronda sin gastar un intento; queda PENDIENTE por si se re-habilita.
            if (!preferencias.avisoHabilitado(pendiente.destinatarioId(), pendiente.tipo().name(),
                    pendiente.canal().name())) {
                continue;
            }
            boolean entregada = false;
            try {
                entregada = canal.entregar(pendiente);
            } catch (Exception ignored) {
                entregada = false;
            }
            OffsetDateTime ahora = OffsetDateTime.now();
            if (entregada) {
                notificaciones.guardar(new Notificacion(pendiente.id(), pendiente.destinatarioId(),
                        pendiente.tipo(), pendiente.aggregateId(), pendiente.eventVersion(), pendiente.detalle(),
                        pendiente.canal(), EstadoEntrega.ENTREGADA, pendiente.intentos() + 1,
                        pendiente.fechaGeneracion(), ahora));
            } else {
                int intentos = pendiente.intentos() + 1;
                notificaciones.guardar(new Notificacion(pendiente.id(), pendiente.destinatarioId(),
                        pendiente.tipo(), pendiente.aggregateId(), pendiente.eventVersion(), pendiente.detalle(),
                        pendiente.canal(), intentos >= 3 ? EstadoEntrega.FALLIDA : EstadoEntrega.PENDIENTE,
                        intentos, pendiente.fechaGeneracion(), null));
            }
        }
    }
}
