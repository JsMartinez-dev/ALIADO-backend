package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.EnvioPushPendiente;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.IniciarEntregaPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.RevalidacionOportunidadPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.IniciarEntregaPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Canal;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.CodigosEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.MensajePush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.VinculoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.DispositivoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.ReclamacionTrabajoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ConsultaPreferenciasPort;
import java.util.Objects;
import java.util.Optional;

/**
 * Revalidación por destinatario antes de llamar al proveedor push (F09-PUSH-08).
 *
 * <p>Se ejecuta dentro de una transacción corta después del commit de la reclamación.
 * Comprueba la vigencia del trabajo, que el aviso siga siendo una oportunidad push, la
 * preferencia del destinatario, que la solicitud siga publicada y compatible y que el
 * vínculo del token siga activo para la misma generación y cuenta. Si algo dejó de ser
 * válido, persiste la supresión de esa entrega y no se contacta al proveedor. El matching
 * global no se repite.</p>
 */
public class IniciarEntregaPushService implements IniciarEntregaPushUseCase {

    private final ReclamacionTrabajoPushRepository reclamaciones;
    private final NotificacionRepository notificaciones;
    private final DispositivoPushRepository dispositivos;
    private final ConsultaPreferenciasPort preferencias;
    private final RevalidacionOportunidadPushPort revalidacion;

    public IniciarEntregaPushService(ReclamacionTrabajoPushRepository reclamaciones,
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
    public Optional<EnvioPushPendiente> ejecutar(IniciarEntregaPushCommand command) {
        ReclamacionPush reclamacion = command.reclamacion();
        Optional<EntregaPush> encontrada = reclamaciones.buscarEntrega(reclamacion.entregaId());
        if (encontrada.isEmpty()) {
            return Optional.empty();
        }
        EntregaPush entrega = encontrada.get();
        if (!entrega.pendiente()
                || !entrega.trabajoId().equals(reclamacion.trabajoId())
                || entrega.generacion() != reclamacion.generacion()) {
            return Optional.empty();
        }

        Optional<TrabajoPush> encontrado = reclamaciones.buscarTrabajo(entrega.trabajoId());
        if (encontrado.isEmpty() || encontrado.get().finalizado()
                || !encontrado.get().preparado()) {
            return Optional.empty();
        }
        TrabajoPush trabajo = encontrado.get();
        if (reclamaciones.trabajoExpirado(trabajo.id())) {
            suprimir(entrega, reclamacion, EstadoEntregaPush.EXPIRADA,
                    CodigosEntregaPush.VIGENCIA_AGOTADA);
            return Optional.empty();
        }

        Optional<Notificacion> aviso = notificaciones.porId(trabajo.notificacionId());
        if (aviso.isEmpty() || aviso.get().tipo() != TipoNotificacion.NUEVA_OPORTUNIDAD
                || aviso.get().canal() != Canal.PUSH) {
            suprimir(entrega, reclamacion, EstadoEntregaPush.SUPRIMIDA,
                    CodigosEntregaPush.TIPO_NO_SOPORTADO);
            return Optional.empty();
        }
        Notificacion notificacion = aviso.get();

        if (!preferencias.avisoHabilitado(notificacion.destinatarioId(),
                TipoNotificacion.NUEVA_OPORTUNIDAD.name(), Canal.PUSH.name())) {
            suprimir(entrega, reclamacion, EstadoEntregaPush.SUPRIMIDA,
                    CodigosEntregaPush.PREFERENCIA_DESHABILITADA);
            return Optional.empty();
        }
        if (!revalidacion.sigueVigente(notificacion.aggregateId(), notificacion.destinatarioId())) {
            suprimir(entrega, reclamacion, EstadoEntregaPush.SUPRIMIDA,
                    CodigosEntregaPush.OPORTUNIDAD_NO_VIGENTE);
            return Optional.empty();
        }

        Optional<VinculoPush> vigente = dispositivos.vinculoActivo(entrega.instalacionId());
        if (vigente.isEmpty() || !vigente.get().activo()
                || !vigente.get().esDe(notificacion.destinatarioId())) {
            suprimir(entrega, reclamacion, EstadoEntregaPush.SUPRIMIDA,
                    CodigosEntregaPush.VINCULO_REVOCADO);
            return Optional.empty();
        }
        VinculoPush vinculo = vigente.get();
        if (vinculo.generacion() != entrega.generacion()) {
            suprimir(entrega, reclamacion, EstadoEntregaPush.SUPRIMIDA,
                    CodigosEntregaPush.GENERACION_OBSOLETA);
            return Optional.empty();
        }
        if (vinculo.tokenFcm() == null || vinculo.tokenFcm().isBlank()) {
            suprimir(entrega, reclamacion, EstadoEntregaPush.SUPRIMIDA,
                    CodigosEntregaPush.VINCULO_REVOCADO);
            return Optional.empty();
        }

        MensajePush mensaje = new MensajePush(notificacion.id(), notificacion.aggregateId(),
                notificacion.tipo(), vinculo.tokenFcm());
        return Optional.of(new EnvioPushPendiente(entrega.id(), trabajo.id(),
                entrega.instalacionId(), entrega.generacion(), reclamacion.reclamacionId(),
                entrega.intentos(), trabajo.expiraEn(), mensaje));
    }

    private void suprimir(EntregaPush entrega, ReclamacionPush reclamacion,
                          EstadoEntregaPush estado, String codigo) {
        // Sin envío no se consume un intento: se conservan los realizados hasta ahora.
        ResultadoEntregaPush supresion = new ResultadoEntregaPush(entrega.id(),
                reclamacion.reclamacionId(), entrega.generacion(), estado, entrega.intentos(),
                null, null, codigo);
        if (reclamaciones.guardarResultadoEntrega(supresion)) {
            reclamaciones.cerrarPadreSiCorresponde(entrega.trabajoId());
        }
    }
}
