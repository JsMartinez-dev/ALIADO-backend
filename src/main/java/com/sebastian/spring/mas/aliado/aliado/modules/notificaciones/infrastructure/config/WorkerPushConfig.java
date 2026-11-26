package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.config;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.EntregaPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.ProcesadorTrabajoPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.ReclamacionTrabajoPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.RevalidacionOportunidadPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.IniciarEntregaPushService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.PrepararEntregasPushService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.ProcesarTrabajoPushService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.RegistrarResultadoEntregaPushService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.IniciarEntregaPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.PrepararEntregasPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ProcesarTrabajoPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RegistrarResultadoEntregaPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.PoliticaReintentoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.VentanaEnvioPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.DispositivoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.ReclamacionTrabajoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.EntregaPushFalsaAdapter;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.ProcesadorTrabajoPushAdapter;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.ReclamacionTrabajoPushAdapter;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.SenalTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm.EntregaPushFirebaseAdapter;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm.EnvioFcm;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm.EnvioFcmFirebase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm.MensajeFcmMapper;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.CoordinadorTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.EscuchaTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.EscuchaTrabajoPushPostgres;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.EsperaReconexionHilo;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.ProgramadorTemporizador;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.ProgramadorTemporizadorEjecutor;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.RetrocesoReconexionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ConsultaPreferenciasPort;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Composición del worker push dirigido por {@code LISTEN}/{@code NOTIFY} (F09-PUSH-06/07/08).
 *
 * <p>El worker queda apagado por defecto. F09-PUSH-07 añade el adaptador de reclamación
 * SQL con {@code FOR UPDATE SKIP LOCKED}, vencimiento y señal transaccional.
 * F09-PUSH-08 aporta el procesador que prepara las entregas por dispositivo, revalida y
 * aplica los resultados con la política de reintento. F09-PUSH-09 añade el adaptador de
 * Firebase detrás de {@link EntregaPushPort}: se compone solo con
 * {@code aliado.push.proveedor=firebase}; en cualquier otro caso se conserva el doble que
 * nunca contacta al proveedor.</p>
 *
 * <p>La conexión de escucha debe apuntar a un endpoint que conserve la sesión PostgreSQL.
 * Si el despliegue usa un pooler en modo transacción, configurar
 * {@code aliado.push.escucha.url} con una conexión directa o un pool de sesión.</p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "aliado.push.worker-habilitado", havingValue = "true")
public class WorkerPushConfig {

    @Bean
    public ProcesarTrabajoPushUseCase procesarTrabajoPushUseCase(ReclamacionTrabajoPushPort reclamacion) {
        return new ProcesarTrabajoPushService(reclamacion);
    }

    /** Reloj del procesamiento push; se puede sustituir en pruebas con un reloj controlado. */
    @Bean
    public Clock relojPush() {
        return Clock.systemUTC();
    }

    /** Backoff exponencial con jitter; el máximo de intentos y la base son configurables. */
    @Bean
    public PoliticaReintentoPush politicaReintentoPush(
            @Value("${aliado.push.reintento.max-intentos:5}") int intentosMaximos,
            @Value("${aliado.push.reintento.base-segundos:30}") long baseSegundos) {
        return new PoliticaReintentoPush(intentosMaximos, Duration.ofSeconds(baseSegundos),
                Math::random);
    }

    /**
     * Ventana mínima para iniciar un envío (D-08): timeout del proveedor más un margen para
     * registrar el resultado. Debe ser menor que la vigencia de la reclamación; si no, una
     * entrega liberada se reclamaría en un bucle de retraso cero sin llegar a enviarse.
     */
    @Bean
    public VentanaEnvioPush ventanaEnvioPush(
            @Value("${aliado.push.envio.timeout-segundos:5}") long timeoutSegundos,
            @Value("${aliado.push.envio.margen-segundos:5}") long margenSegundos,
            @Value("${aliado.push.reclamacion.vigencia-segundos:45}") long vigenciaSegundos) {
        VentanaEnvioPush ventana = new VentanaEnvioPush(Duration.ofSeconds(timeoutSegundos),
                Duration.ofSeconds(margenSegundos));
        if (ventana.requerido().compareTo(Duration.ofSeconds(vigenciaSegundos)) >= 0) {
            throw new IllegalStateException("El timeout de envío y su margen deben ser menores"
                    + " que la vigencia de la reclamación");
        }
        return ventana;
    }

    @Bean
    public PrepararEntregasPushUseCase prepararEntregasPushUseCase(
            ReclamacionTrabajoPushRepository reclamaciones,
            NotificacionRepository notificaciones,
            DispositivoPushRepository dispositivos,
            ConsultaPreferenciasPort preferencias,
            RevalidacionOportunidadPushPort revalidacion) {
        return new PrepararEntregasPushService(reclamaciones, notificaciones, dispositivos,
                preferencias, revalidacion);
    }

    @Bean
    public IniciarEntregaPushUseCase iniciarEntregaPushUseCase(
            ReclamacionTrabajoPushRepository reclamaciones,
            NotificacionRepository notificaciones,
            DispositivoPushRepository dispositivos,
            ConsultaPreferenciasPort preferencias,
            RevalidacionOportunidadPushPort revalidacion,
            VentanaEnvioPush ventana,
            Clock relojPush) {
        return new IniciarEntregaPushService(reclamaciones, notificaciones, dispositivos,
                preferencias, revalidacion, ventana, relojPush);
    }

    @Bean
    public RegistrarResultadoEntregaPushUseCase registrarResultadoEntregaPushUseCase(
            ReclamacionTrabajoPushRepository reclamaciones,
            DispositivoPushRepository dispositivos,
            PoliticaReintentoPush politica,
            Clock relojPush) {
        return new RegistrarResultadoEntregaPushService(reclamaciones, dispositivos, politica,
                relojPush);
    }

    /**
     * Doble de entrega local y para las pruebas. F09-PUSH-09 compone el adaptador de
     * Firebase con {@code aliado.push.proveedor=firebase}; el doble nunca contacta con la
     * red ni marca aceptaciones falsas.
     */
    @Bean
    @ConditionalOnProperty(name = "aliado.push.proveedor", havingValue = "falso",
            matchIfMissing = true)
    public EntregaPushPort entregaPushFalsa() {
        return new EntregaPushFalsaAdapter();
    }

    /**
     * Transporte FCM con el timeout de envío de D-08. Comparte la propiedad con
     * {@link #ventanaEnvioPush} para que la guardia de la reclamación y la espera al
     * proveedor no puedan divergir. Las credenciales las aporta {@link FirebasePushConfig}.
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "aliado.push.proveedor", havingValue = "firebase")
    public EnvioFcm envioFcmFirebase(FirebaseApp firebaseAppPush,
            @Value("${aliado.push.envio.timeout-segundos:5}") long timeoutSegundos) {
        return EnvioFcmFirebase.conFirebase(FirebaseMessaging.getInstance(firebaseAppPush),
                Duration.ofSeconds(timeoutSegundos));
    }

    /**
     * Adaptador de entrega FCM. Solo lo habilita el piloto con
     * {@code aliado.push.proveedor=firebase}; el TTL acota lo que FCM puede retener y el
     * canal es el acordado con Android.
     */
    @Bean
    @ConditionalOnProperty(name = "aliado.push.proveedor", havingValue = "firebase")
    public EntregaPushPort entregaPushFirebase(EnvioFcm envioFcmFirebase,
            @Value("${aliado.push.firebase.ttl-segundos:3600}") long ttlSegundos,
            @Value("${aliado.push.firebase.canal-android:aliado_oportunidades}") String canalAndroid) {
        return new EntregaPushFirebaseAdapter(envioFcmFirebase,
                new MensajeFcmMapper(Duration.ofSeconds(ttlSegundos), canalAndroid));
    }

    /**
     * Procesador de la reclamación. Cada fase abre su propia transacción corta; la llamada
     * al proveedor queda fuera de todas ellas.
     */
    @Bean
    public ProcesadorTrabajoPushPort procesadorTrabajoPushPort(
            PrepararEntregasPushUseCase preparar,
            IniciarEntregaPushUseCase iniciar,
            RegistrarResultadoEntregaPushUseCase registrar,
            EntregaPushPort entrega,
            PlatformTransactionManager transactionManager) {
        TransactionTemplate transaccion = new TransactionTemplate(transactionManager);
        transaccion.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return new ProcesadorTrabajoPushAdapter(preparar, iniciar, registrar, entrega, transaccion);
    }

    /**
     * Reclamación con transacción corta propia: el commit de la reclamación y su
     * {@code NOTIFY} ocurren antes de procesar el lote y de cualquier llamada externa.
     */
    @Bean
    public ReclamacionTrabajoPushPort reclamacionTrabajoPushPort(
            ReclamacionTrabajoPushRepository reclamaciones,
            ProcesadorTrabajoPushPort procesador,
            PlatformTransactionManager transactionManager,
            @Value("${aliado.push.reclamacion.tamano-lote:10}") int tamanoLote,
            @Value("${aliado.push.reclamacion.vigencia-segundos:45}") long vigenciaSegundos) {
        TransactionTemplate transaccion = new TransactionTemplate(transactionManager);
        transaccion.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return new ReclamacionTrabajoPushAdapter(reclamaciones, procesador, transaccion,
                tamanoLote, Duration.ofSeconds(vigenciaSegundos));
    }

    /** Sesión de escucha dedicada, fuera del pool de consultas de la aplicación. */
    @Bean(destroyMethod = "close")
    public EscuchaTrabajoPush escuchaTrabajoPush(
            @Value("${aliado.push.escucha.url}") String url,
            @Value("${aliado.push.escucha.username:}") String usuario,
            @Value("${aliado.push.escucha.password:}") String contrasena) {
        return new EscuchaTrabajoPushPostgres(url, usuario, contrasena, SenalTrabajoPush.CANAL);
    }

    @Bean(destroyMethod = "close")
    public ProgramadorTemporizador programadorTemporizadorPush() {
        return new ProgramadorTemporizadorEjecutor();
    }

    @Bean(initMethod = "iniciar", destroyMethod = "cerrar")
    public CoordinadorTrabajoPush coordinadorTrabajoPush(
            ProcesarTrabajoPushUseCase ciclo,
            ProgramadorTemporizador temporizador,
            EscuchaTrabajoPush escucha,
            @Value("${aliado.push.reconexion.base-ms:500}") long retrocesoBaseMs,
            @Value("${aliado.push.reconexion.max-ms:15000}") long retrocesoMaximoMs) {
        return new CoordinadorTrabajoPush(ciclo, escucha, temporizador,
                new RetrocesoReconexionPush(Duration.ofMillis(retrocesoBaseMs),
                        Duration.ofMillis(retrocesoMaximoMs)),
                new EsperaReconexionHilo(), Clock.systemUTC());
    }
}
