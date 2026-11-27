package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.EntregarAvisoBandejaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.CanalEntregaNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.EntregarAvisoBandejaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntrega;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ConsultaPreferenciasPort;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Entrega por canal de un aviso de bandeja recién registrado (F09-PUSH-11).
 *
 * <p>Reproduce, aviso a aviso, el comportamiento que tenía el worker de 60 segundos:
 * conserva los intentos y el estado del aviso, salta sin gastar intentos cuando el canal
 * no tiene proveedor real para ese tipo o la preferencia dejó de estar habilitada, y solo
 * consulta el aviso indicado por el evento. No lee la cola ni busca pendientes: los
 * tipos con proveedor push (solo {@code NUEVA_OPORTUNIDAD} en el piloto) se entregan por
 * la cola durable, no por este canal.</p>
 */
public class EntregarAvisoBandejaService implements EntregarAvisoBandejaUseCase {

    private static final int INTENTOS_MAXIMOS = 3;

    private final NotificacionRepository notificaciones;
    private final CanalEntregaNotificacion canal;
    private final ConsultaPreferenciasPort preferencias;
    private final Clock reloj;

    public EntregarAvisoBandejaService(NotificacionRepository notificaciones,
                                       CanalEntregaNotificacion canal,
                                       ConsultaPreferenciasPort preferencias,
                                       Clock reloj) {
        this.notificaciones = Objects.requireNonNull(notificaciones);
        this.canal = Objects.requireNonNull(canal);
        this.preferencias = Objects.requireNonNull(preferencias);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Override
    public void ejecutar(EntregarAvisoBandejaCommand command) {
        Optional<Notificacion> encontrada = notificaciones.porId(command.notificacionId());
        if (encontrada.isEmpty()) {
            return;
        }
        Notificacion aviso = encontrada.get();
        if (aviso.estadoEntrega() != EstadoEntrega.PENDIENTE) {
            return;
        }
        // Un tipo auditado sin proveedor real conserva el aviso en la bandeja sin gastar
        // intentos: su entrega la resolverá el canal que corresponda, nunca un sondeo.
        if (!canal.disponibleParaEntrega(aviso)) {
            return;
        }
        // La preferencia pudo cambiar entre el registro y el commit.
        if (!preferencias.avisoHabilitado(aviso.destinatarioId(), aviso.tipo().name(),
                aviso.canal().name())) {
            return;
        }

        boolean entregada = false;
        try {
            entregada = canal.entregar(aviso);
        } catch (RuntimeException falloDelCanal) {
            entregada = false;
        }
        int intentos = aviso.intentos() + 1;
        EstadoEntrega estado = entregada
                ? EstadoEntrega.ENTREGADA
                : (intentos >= INTENTOS_MAXIMOS ? EstadoEntrega.FALLIDA : EstadoEntrega.PENDIENTE);
        notificaciones.guardar(new Notificacion(aviso.id(), aviso.destinatarioId(), aviso.tipo(),
                aviso.aggregateId(), aviso.eventVersion(), aviso.detalle(), aviso.canal(), estado,
                intentos, aviso.fechaGeneracion(),
                entregada ? OffsetDateTime.now(reloj) : null, aviso.leidaEn()));
    }
}
