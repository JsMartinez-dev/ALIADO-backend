package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.EnvioPushPendiente;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.IniciarEntregaPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.MetricasPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.RevalidacionOportunidadPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.IniciarEntregaPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Canal;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.CodigosEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ConfiguracionCanalPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.MensajePush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.VentanaEnvioPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.VinculoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.DispositivoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.ReclamacionTrabajoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ConsultaPreferenciasPort;
import java.time.Clock;
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
 *
 * <p>Antes de entregar el envío al procesador exige que el tiempo restante de la
 * reclamación alcance el timeout del proveedor más un margen para registrar el resultado
 * (D-08). Si no alcanza, libera la reclamación sin enviar ni marcar resultado: la entrega
 * vuelve a ser reclamable con un vencimiento nuevo en lugar de arriesgar un resultado
 * tardío que se descartaría y provocaría un reenvío.</p>
 */
public class IniciarEntregaPushService implements IniciarEntregaPushUseCase {

    private final ReclamacionTrabajoPushRepository reclamaciones;
    private final NotificacionRepository notificaciones;
    private final DispositivoPushRepository dispositivos;
    private final ConsultaPreferenciasPort preferencias;
    private final RevalidacionOportunidadPushPort revalidacion;
    private final VentanaEnvioPush ventana;
    private final ConfiguracionCanalPush configuracion;
    private final MetricasPushPort metricas;
    private final Clock reloj;

    public IniciarEntregaPushService(ReclamacionTrabajoPushRepository reclamaciones,
                                     NotificacionRepository notificaciones,
                                     DispositivoPushRepository dispositivos,
                                     ConsultaPreferenciasPort preferencias,
                                     RevalidacionOportunidadPushPort revalidacion,
                                     VentanaEnvioPush ventana,
                                     ConfiguracionCanalPush configuracion,
                                     MetricasPushPort metricas,
                                     Clock reloj) {
        this.reclamaciones = Objects.requireNonNull(reclamaciones);
        this.notificaciones = Objects.requireNonNull(notificaciones);
        this.dispositivos = Objects.requireNonNull(dispositivos);
        this.preferencias = Objects.requireNonNull(preferencias);
        this.revalidacion = Objects.requireNonNull(revalidacion);
        this.ventana = Objects.requireNonNull(ventana);
        this.configuracion = Objects.requireNonNull(configuracion);
        this.metricas = Objects.requireNonNull(metricas);
        this.reloj = Objects.requireNonNull(reloj);
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

        // Apagado controlado del canal: la entrega se suprime sin llamar al proveedor y el
        // padre se cierra cuando corresponda. La bandeja conserva el aviso ya registrado.
        if (!configuracion.habilitadoPara(notificacion.tipo())) {
            suprimir(entrega, reclamacion, EstadoEntregaPush.SUPRIMIDA,
                    CodigosEntregaPush.CANAL_NO_HABILITADO);
            return Optional.empty();
        }

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

        // Guardia de la ventana de envío (D-08): sin tiempo para el timeout y el margen del
        // resultado, no se llama al proveedor. La reclamación se libera sin marcar nada y
        // la fila vuelve a ser reclamable con un vencimiento nuevo.
        if (!ventana.alcanza(reclamacion.venceEn(), reloj.instant())) {
            reclamaciones.liberarReclamacionDeEntrega(entrega.id(), reclamacion.reclamacionId());
            return Optional.empty();
        }

        MensajePush mensaje = new MensajePush(notificacion.id(), notificacion.aggregateId(),
                notificacion.tipo(), vinculo.tokenFcm());
        return Optional.of(new EnvioPushPendiente(entrega.id(), trabajo.id(),
                entrega.instalacionId(), entrega.generacion(), reclamacion.reclamacionId(),
                entrega.intentos(), trabajo.expiraEn(), trabajo.encoladoEn(), mensaje));
    }

    private void suprimir(EntregaPush entrega, ReclamacionPush reclamacion,
                          EstadoEntregaPush estado, String codigo) {
        // Sin envío no se consume un intento: se conservan los realizados hasta ahora.
        ResultadoEntregaPush supresion = new ResultadoEntregaPush(entrega.id(),
                reclamacion.reclamacionId(), entrega.generacion(), estado, entrega.intentos(),
                null, null, codigo);
        if (reclamaciones.guardarResultadoEntrega(supresion)) {
            metricas.fallo(codigo);
            reclamaciones.cerrarPadreSiCorresponde(entrega.trabajoId());
        }
    }
}
