package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.config;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.ReclamacionTrabajoPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.ProcesarTrabajoPushService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ProcesarTrabajoPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.SenalTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.CoordinadorTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.EscuchaTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.EscuchaTrabajoPushPostgres;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.EsperaReconexionHilo;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.ProgramadorTemporizador;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.ProgramadorTemporizadorEjecutor;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.RetrocesoReconexionPush;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composición del worker push dirigido por {@code LISTEN}/{@code NOTIFY} (F09-PUSH-06).
 *
 * <p>El worker queda apagado por defecto: el pedido F09-PUSH-06 solo aporta la escucha,
 * el coordinador y el temporizador. El adaptador de reclamación llega en F09-PUSH-07 y el
 * despacho real en F09-PUSH-08; al existir el bean {@link ReclamacionTrabajoPushPort} se
 * puede activar con {@code aliado.push.worker-habilitado=true}.</p>
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
