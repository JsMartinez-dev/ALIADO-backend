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


@Service
public class NotificacionService implements NotificacionPort, NotificacionConsultaPort, EntregaNotificacionesPort {
    private final NotificacionRepository notificacionRepository;
    private final CanalEntregaNotificacion canalEntregaNotificacion;
    private final ConsultaPreferenciasPort consultaPreferenciasPort;

    public NotificacionService(NotificacionRepository notificacionRepository,
            CanalEntregaNotificacion canalEntregaNotificacion,
            ConsultaPreferenciasPort consultaPreferenciasPort) {
        this.notificacionRepository = notificacionRepository;
        this.canalEntregaNotificacion = canalEntregaNotificacion;
        this.consultaPreferenciasPort = consultaPreferenciasPort;
    }

    @Override
    @Transactional
    public void registrar(TipoNotificacion tipoNotificacion, UUID agregadoId, UUID destinatarioId,
            int versionEvento, String detalleNotificacion, Canal canalPreferido) {
        if (notificacionRepository.existeEvento(
                tipoNotificacion.name(), agregadoId, destinatarioId, versionEvento)) {
            return; // Idempotencia por evento (plan general 19.3): no duplicar avisos.
        }
        Canal canalEfectivo = canalPreferido == null ? Canal.PUSH : canalPreferido;
        // SPEC-14 (F14-T004): no registrar avisos de un tipo/canal que el usuario desactivó.
        if (!consultaPreferenciasPort.avisoHabilitado(
                destinatarioId, tipoNotificacion.name(), canalEfectivo.name())) {
            return;
        }
        Notificacion notificacion = new Notificacion(UUID.randomUUID(), destinatarioId, tipoNotificacion, agregadoId,
                versionEvento, detalleNotificacion, canalEfectivo, EstadoEntrega.PENDIENTE, 0,
                OffsetDateTime.now(), null);
        notificacionRepository.guardar(notificacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionConsulta> listarPropias(UUID destinatarioId) {
        return notificacionRepository.porDestinatario(destinatarioId).stream().map(this::aConsulta).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionConsulta> listarPorTipo(UUID destinatarioId, TipoNotificacion tipo) {
        return notificacionRepository.porDestinatarioYTipo(destinatarioId, tipo.name()).stream()
                .map(this::aConsulta).toList();
    }

    private NotificacionConsulta aConsulta(Notificacion notificacion) {
        UUID servicioId = (notificacion.tipo() == TipoNotificacion.ACEPTACION_PROPUESTA
                || notificacion.tipo() == TipoNotificacion.CAMBIO_ESTADO_SERVICIO)
                ? notificacion.aggregateId() : null;
        // SPEC-06: el aggregateId de NUEVA_OPORTUNIDAD ES la solicitud que la originó.
        UUID solicitudId = notificacion.tipo() == TipoNotificacion.NUEVA_OPORTUNIDAD
                ? notificacion.aggregateId() : null;
        return new NotificacionConsulta(notificacion.id(), notificacion.tipo(), servicioId, solicitudId,
                notificacion.detalle(), notificacion.estadoEntrega(), notificacion.fechaGeneracion());
    }

    /** Worker de entrega: reintenta PENDIENTE/FALLIDA; a los 3 intentos queda FALLIDA. */
    @Override
    @Transactional
    public void entregarPendientes() {
        for (Notificacion notificacionPendiente : notificacionRepository.pendientesParaEntrega()) {
            // UC038: sin proveedor real la oportunidad continúa pendiente. No se
            // registra un intento fallido ni se repite un log cada ronda del scheduler.
            if (!canalEntregaNotificacion.disponibleParaEntrega(notificacionPendiente)) {
                continue;
            }
            // La preferencia pudo cambiar desde el registro: si ya no está habilitada,
            // se salta la ronda sin gastar un intento; queda PENDIENTE por si se re-habilita.
            if (!consultaPreferenciasPort.avisoHabilitado(notificacionPendiente.destinatarioId(),
                    notificacionPendiente.tipo().name(), notificacionPendiente.canal().name())) {
                continue;
            }
            boolean entregaExitosa = false;
            try {
                entregaExitosa = canalEntregaNotificacion.entregar(notificacionPendiente);
            } catch (Exception ignored) {
                entregaExitosa = false;
            }
            OffsetDateTime fechaHoraEntrega = OffsetDateTime.now();
            if (entregaExitosa) {
                notificacionRepository.guardar(new Notificacion(notificacionPendiente.id(),
                        notificacionPendiente.destinatarioId(), notificacionPendiente.tipo(),
                        notificacionPendiente.aggregateId(), notificacionPendiente.eventVersion(),
                        notificacionPendiente.detalle(), notificacionPendiente.canal(), EstadoEntrega.ENTREGADA,
                        notificacionPendiente.intentos() + 1, notificacionPendiente.fechaGeneracion(),
                        fechaHoraEntrega));
            } else {
                int intentosEntrega = notificacionPendiente.intentos() + 1;
                notificacionRepository.guardar(new Notificacion(notificacionPendiente.id(),
                        notificacionPendiente.destinatarioId(), notificacionPendiente.tipo(),
                        notificacionPendiente.aggregateId(), notificacionPendiente.eventVersion(),
                        notificacionPendiente.detalle(), notificacionPendiente.canal(),
                        intentosEntrega >= 3 ? EstadoEntrega.FALLIDA : EstadoEntrega.PENDIENTE,
                        intentosEntrega, notificacionPendiente.fechaGeneracion(), null));
            }
        }
    }
}
