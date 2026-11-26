package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.PrepararEntregasPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.RevalidacionOportunidadPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.PrepararEntregasPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Canal;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.DestinoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.MotivoCierreTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.VinculoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.DispositivoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.ReclamacionTrabajoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ConsultaPreferenciasPort;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Preparación idempotente de las entregas de un trabajo padre (F09-PUSH-08).
 *
 * <p>El caso de uso se ejecuta dentro de la transacción corta que abre el adaptador del
 * procesador, después de confirmar la reclamación. Reutiliza el aviso ya persistido: no
 * vuelve a ejecutar el matching global ni crea avisos. Si la oportunidad, la preferencia
 * o la vigencia dejaron de ser válidas, cierra el padre con su motivo; si no hay
 * dispositivos elegibles, también. En caso contrario confirma primero el padre y después
 * crea una entrega por vínculo activo en la misma transacción: si la reclamación venció,
 * no queda ninguna entrega creada y el padre se reintenta al vencer su plazo.</p>
 */
public class PrepararEntregasPushService implements PrepararEntregasPushUseCase {

    private final ReclamacionTrabajoPushRepository reclamaciones;
    private final NotificacionRepository notificaciones;
    private final DispositivoPushRepository dispositivos;
    private final ConsultaPreferenciasPort preferencias;
    private final RevalidacionOportunidadPushPort revalidacion;

    public PrepararEntregasPushService(ReclamacionTrabajoPushRepository reclamaciones,
                                       NotificacionRepository notificaciones,
                                       DispositivoPushRepository dispositivos,
                                       ConsultaPreferenciasPort preferencias,
                                       RevalidacionOportunidadPushPort revalidacion) {
        this.reclamaciones = Objects.requireNonNull(reclamaciones);
        this.notificaciones = Objects.requireNonNull(notificaciones);
        this.dispositivos = Objects.requireNonNull(dispositivos);
        this.preferencias = Objects.requireNonNull(preferencias);
        this.revalidacion = Objects.requireNonNull(revalidacion);
    }

    @Override
    public void ejecutar(PrepararEntregasPushCommand command) {
        ReclamacionPush reclamacion = command.reclamacion();
        Optional<TrabajoPush> encontrado = reclamaciones.buscarTrabajo(reclamacion.trabajoId());
        if (encontrado.isEmpty() || encontrado.get().estado() != EstadoTrabajoPush.PENDIENTE_PREPARACION) {
            return;
        }
        TrabajoPush trabajo = encontrado.get();
        if (reclamaciones.trabajoExpirado(trabajo.id())) {
            finalizar(trabajo, reclamacion, MotivoCierreTrabajoPush.VIGENCIA_AGOTADA);
            return;
        }

        Optional<Notificacion> aviso = notificaciones.porId(trabajo.notificacionId());
        if (aviso.isEmpty() || aviso.get().tipo() != TipoNotificacion.NUEVA_OPORTUNIDAD
                || aviso.get().canal() != Canal.PUSH) {
            finalizar(trabajo, reclamacion, MotivoCierreTrabajoPush.TIPO_NO_SOPORTADO);
            return;
        }
        Notificacion notificacion = aviso.get();

        if (!preferencias.avisoHabilitado(notificacion.destinatarioId(),
                TipoNotificacion.NUEVA_OPORTUNIDAD.name(), Canal.PUSH.name())) {
            finalizar(trabajo, reclamacion, MotivoCierreTrabajoPush.PREFERENCIA_DESHABILITADA);
            return;
        }
        if (!revalidacion.sigueVigente(notificacion.aggregateId(), notificacion.destinatarioId())) {
            finalizar(trabajo, reclamacion, MotivoCierreTrabajoPush.OPORTUNIDAD_NO_VIGENTE);
            return;
        }

        List<DestinoPush> destinos = dispositivos.vinculosActivosDe(notificacion.destinatarioId()).stream()
                .filter(VinculoPush::activo)
                .filter(vinculo -> vinculo.tokenFcm() != null && !vinculo.tokenFcm().isBlank())
                .map(vinculo -> new DestinoPush(vinculo.instalacionId(), vinculo.generacion()))
                .distinct()
                .toList();
        if (destinos.isEmpty()) {
            finalizar(trabajo, reclamacion, MotivoCierreTrabajoPush.SIN_DISPOSITIVOS_ELEGIBLES);
            return;
        }

        // La confirmación va primero: si la reclamación venció, no se crea ninguna entrega y
        // la preparación se reintenta cuando la fila vuelva a ser reclamable. Ambas
        // escrituras comparten transacción, así que un fallo al crear las entregas revierte
        // también la confirmación y el padre no queda PREPARADO sin entregas.
        if (!reclamaciones.confirmarPreparacion(trabajo.id(), reclamacion.reclamacionId())) {
            return;
        }
        reclamaciones.crearEntregas(trabajo.id(), destinos);
        // Si las entregas ya existían y eran terminales, el padre no debe quedarse
        // PREPARADO sin trabajo pendiente.
        reclamaciones.cerrarPadreSiCorresponde(trabajo.id());
    }

    private void finalizar(TrabajoPush trabajo, ReclamacionPush reclamacion,
                           MotivoCierreTrabajoPush motivo) {
        reclamaciones.finalizarTrabajo(trabajo.id(), reclamacion.reclamacionId(), motivo.name());
    }
}
