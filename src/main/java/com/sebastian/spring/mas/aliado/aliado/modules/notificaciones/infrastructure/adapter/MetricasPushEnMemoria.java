package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResumenMetricasPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.MetricasPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.PendientesPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoReclamacionPush;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registro en memoria de las métricas del ciclo push (F09-PUSH-11).
 *
 * <p>No consulta la base de datos: todos los contadores se alimentan con los eventos del
 * worker y del encolado. El tamaño de la cola solo se actualiza cuando el worker termina
 * un lote con trabajo, de modo que la instrumentación tampoco consulta la cola en
 * reposo.</p>
 *
 * <p>Al registrar {@link #pendientes(PendientesPush)} se emite una única línea de
 * resumen, que es la superficie de inspección operativa junto con el acceso directo a
 * {@link #resumen()}. Ni la línea ni el resumen contienen tokens, instalaciones ni
 * contenido de los avisos.</p>
 */
public class MetricasPushEnMemoria implements MetricasPushPort {

    private static final Logger log = LoggerFactory.getLogger(MetricasPushEnMemoria.class);

    private final Clock reloj;
    private final Map<String, LongAdder> encolados = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> reclamaciones = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> reintentos = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> aceptaciones = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> fallos = new ConcurrentHashMap<>();
    private final LongAdder aceptacionesTotales = new LongAdder();
    private final AtomicLong latenciaTotalMs = new AtomicLong();
    private final AtomicLong latenciaMaximaMs = new AtomicLong();
    private volatile PendientesPush pendientes = PendientesPush.vacio();

    public MetricasPushEnMemoria(Clock reloj) {
        this.reloj = Objects.requireNonNull(reloj, "El reloj de las métricas es obligatorio");
    }

    @Override
    public void encolado(TipoNotificacion tipo) {
        incrementar(encolados, clave(tipo));
    }

    @Override
    public void reclamacion(TipoReclamacionPush tipo, int cantidad) {
        if (cantidad <= 0) {
            return;
        }
        incrementar(reclamaciones, Objects.requireNonNull(tipo).name(), cantidad);
    }

    @Override
    public void reintento(TipoNotificacion tipo, int intentos) {
        incrementar(reintentos, clave(tipo));
    }

    @Override
    public void aceptacion(TipoNotificacion tipo, Duration latencia) {
        incrementar(aceptaciones, clave(tipo));
        aceptacionesTotales.increment();
        long milisegundos = latencia == null ? 0 : Math.max(0, latencia.toMillis());
        latenciaTotalMs.addAndGet(milisegundos);
        latenciaMaximaMs.accumulateAndGet(milisegundos, Math::max);
    }

    @Override
    public void fallo(String codigo) {
        String clave = codigo == null || codigo.isBlank() ? "SIN_CODIGO" : codigo;
        incrementar(fallos, clave);
    }

    @Override
    public void pendientes(PendientesPush estado) {
        this.pendientes = Objects.requireNonNull(estado);
        log.info("push.metricas ciclo {}", resumen().describir());
    }

    /** Instantánea consistente de los contadores acumulados por esta instancia. */
    public ResumenMetricasPush resumen() {
        long totales = aceptacionesTotales.sum();
        long mediaMs = totales == 0 ? 0 : latenciaTotalMs.get() / totales;
        PendientesPush estado = pendientes;
        return new ResumenMetricasPush(copiar(encolados), copiar(reclamaciones), copiar(reintentos),
                copiar(aceptaciones), copiar(fallos), estado.trabajosAbiertos(),
                estado.entregasPendientes(), Duration.ofMillis(mediaMs),
                Duration.ofMillis(latenciaMaximaMs.get()), estado.antiguedadEn(reloj.instant()));
    }

    private static String clave(TipoNotificacion tipo) {
        return Objects.requireNonNull(tipo, "El tipo de notificación es obligatorio").name();
    }

    private static void incrementar(Map<String, LongAdder> contadores, String clave) {
        incrementar(contadores, clave, 1);
    }

    private static void incrementar(Map<String, LongAdder> contadores, String clave, long cantidad) {
        contadores.computeIfAbsent(clave, ignorada -> new LongAdder()).add(cantidad);
    }

    private static Map<String, Long> copiar(Map<String, LongAdder> contadores) {
        Map<String, Long> copia = new TreeMap<>();
        contadores.forEach((clave, valor) -> copia.put(clave, valor.sum()));
        return copia;
    }
}
