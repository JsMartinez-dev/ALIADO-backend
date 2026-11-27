package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.EnvioPushPendiente;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RegistrarResultadoEntregaPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.MetricasPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RegistrarResultadoEntregaPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.CodigosEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.PoliticaReintentoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEnvioPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoResultadoEnvioPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.DispositivoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.ReclamacionTrabajoPushRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * Aplica el resultado del proveedor a una entrega y cierra el padre si corresponde
 * (F09-PUSH-08).
 *
 * <p>Un error temporal solo reprograma esa entrega con el máximo entre {@code Retry-After}
 * y el backoff con jitter. Si el siguiente plazo alcanza la vigencia del trabajo, la
 * entrega termina {@code EXPIRADA}; si se agotaron los intentos, termina
 * {@code FALLIDA_DEFINITIVA}. Un token desregistrado revoca únicamente la generación
 * enviada. La escritura se condiciona a la reclamación vigente y la generación del token;
 * tras aplicarla se comprueba el cierre del padre bajo su bloqueo, de modo que dos
 * resultados concurrentes no lo dejen abierto.</p>
 */
public class RegistrarResultadoEntregaPushService implements RegistrarResultadoEntregaPushUseCase {

    private final ReclamacionTrabajoPushRepository reclamaciones;
    private final DispositivoPushRepository dispositivos;
    private final PoliticaReintentoPush politica;
    private final MetricasPushPort metricas;
    private final Clock reloj;

    public RegistrarResultadoEntregaPushService(ReclamacionTrabajoPushRepository reclamaciones,
                                                DispositivoPushRepository dispositivos,
                                                PoliticaReintentoPush politica,
                                                MetricasPushPort metricas,
                                                Clock reloj) {
        this.reclamaciones = Objects.requireNonNull(reclamaciones);
        this.dispositivos = Objects.requireNonNull(dispositivos);
        this.politica = Objects.requireNonNull(politica);
        this.metricas = Objects.requireNonNull(metricas);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Override
    public void ejecutar(RegistrarResultadoEntregaPushCommand command) {
        EnvioPushPendiente envio = command.envio();
        ResultadoEnvioPush resultado = command.resultado();
        int intentos = envio.intentos() + 1;

        EstadoEntregaPush estado;
        Instant proximoIntento = null;
        String idMensaje = null;
        String codigo = null;
        boolean revocarToken = false;

        switch (resultado.tipo()) {
            case ACEPTADO -> {
                estado = EstadoEntregaPush.ACEPTADA;
                idMensaje = resultado.idMensaje();
            }
            case PERMANENTE -> {
                estado = EstadoEntregaPush.FALLIDA_DEFINITIVA;
                codigo = CodigosEntregaPush.normalizar(resultado.codigo(),
                        CodigosEntregaPush.PROVEEDOR_PERMANENTE);
            }
            case TOKEN_DESREGISTRADO -> {
                estado = EstadoEntregaPush.SUPRIMIDA;
                codigo = CodigosEntregaPush.TOKEN_DESREGISTRADO;
                revocarToken = true;
            }
            case TEMPORAL -> {
                if (politica.intentosAgotados(intentos)) {
                    estado = EstadoEntregaPush.FALLIDA_DEFINITIVA;
                    codigo = CodigosEntregaPush.INTENTOS_AGOTADOS;
                } else {
                    Instant candidato = politica.proximoIntento(reloj.instant(), intentos,
                            resultado.reintentarEn());
                    if (!candidato.isBefore(envio.expiraEn())) {
                        estado = EstadoEntregaPush.EXPIRADA;
                        codigo = CodigosEntregaPush.VIGENCIA_AGOTADA;
                    } else {
                        estado = EstadoEntregaPush.PENDIENTE;
                        proximoIntento = candidato;
                        codigo = CodigosEntregaPush.normalizar(resultado.codigo(),
                                CodigosEntregaPush.PROVEEDOR_TEMPORAL);
                    }
                }
            }
            default -> throw new IllegalStateException("Resultado de proveedor no soportado");
        }

        ResultadoEntregaPush persistido = new ResultadoEntregaPush(envio.entregaId(),
                envio.reclamacionId(), envio.generacion(), estado, intentos, proximoIntento,
                idMensaje, codigo);
        if (reclamaciones.guardarResultadoEntrega(persistido)) {
            registrarMetrica(envio, persistido);
            // Solo se revoca la generación enviada y únicamente si el resultado se aplicó a
            // una reclamación todavía vigente; una generación nueva queda intacta.
            if (revocarToken) {
                dispositivos.revocarVinculo(envio.instalacionId(), envio.generacion(),
                        OffsetDateTime.ofInstant(reloj.instant(), ZoneOffset.UTC));
            }
            reclamaciones.cerrarPadreSiCorresponde(envio.trabajoId());
        }
    }

    /**
     * Instrumentación (F09-PUSH-11): solo se cuentan resultados realmente aplicados, para
     * que un resultado tardío descartado no infle la aceptación ni los reintentos.
     */
    private void registrarMetrica(EnvioPushPendiente envio, ResultadoEntregaPush persistido) {
        TipoNotificacion tipo = envio.mensaje().tipo();
        switch (persistido.estado()) {
            case ACEPTADA -> metricas.aceptacion(tipo,
                    Duration.between(envio.encoladoEn(), reloj.instant()));
            case PENDIENTE -> metricas.reintento(tipo, persistido.intentos());
            default -> metricas.fallo(persistido.ultimoCodigoError());
        }
    }
}
