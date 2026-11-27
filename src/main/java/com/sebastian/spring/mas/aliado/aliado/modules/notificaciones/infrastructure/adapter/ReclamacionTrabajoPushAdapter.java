package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResultadoLotePush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.MetricasPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.ProcesadorTrabajoPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.ReclamacionTrabajoPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.LoteReclamadoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.ReclamacionTrabajoPushRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Implementación del puerto que consume el coordinador del worker push (F09-PUSH-07).
 *
 * <p>Cada iteración abre una transacción corta con {@code REQUIRES_NEW} que reclama el
 * lote con {@code FOR UPDATE SKIP LOCKED}, escribe identificador y vencimiento y emite
 * {@code NOTIFY}; al confirmar, el lote se procesa fuera de esa transacción mediante
 * {@link ProcesadorTrabajoPushPort}. La preparación por dispositivo, la revalidación y el
 * envío real los aporta la composición de F09-PUSH-08; el proveedor Firebase llega en
 * F09-PUSH-09.</p>
 *
 * <p>Si alguna de las dos colas agotó su cupo, informa que resta trabajo para drenar de
 * inmediato. Los resultados que se guarden después del vencimiento o de un reemplazo
 * afectan cero filas: la reclamación vencida vuelve a ser elegible al cumplirse su plazo,
 * sin depender de una publicación nueva.</p>
 */
public class ReclamacionTrabajoPushAdapter implements ReclamacionTrabajoPushPort {

    private final ReclamacionTrabajoPushRepository reclamaciones;
    private final ProcesadorTrabajoPushPort procesador;
    private final MetricasPushPort metricas;
    private final TransactionTemplate transaccion;
    private final int maximo;
    private final Duration vencimiento;

    public ReclamacionTrabajoPushAdapter(ReclamacionTrabajoPushRepository reclamaciones,
                                         ProcesadorTrabajoPushPort procesador,
                                         MetricasPushPort metricas,
                                         TransactionTemplate transaccion,
                                         int maximo, Duration vencimiento) {
        if (maximo < 1) {
            throw new IllegalArgumentException("El tamaño del lote debe ser positivo");
        }
        if (vencimiento == null || vencimiento.isZero() || vencimiento.isNegative()) {
            throw new IllegalArgumentException("La vigencia de la reclamación debe ser positiva");
        }
        this.reclamaciones = reclamaciones;
        this.procesador = procesador;
        this.metricas = metricas;
        this.transaccion = transaccion;
        this.maximo = maximo;
        this.vencimiento = vencimiento;
    }

    @Override
    public ResultadoLotePush procesarLoteElegible() {
        LoteReclamadoPush lote = transaccion.execute(estado ->
                reclamaciones.reclamar(maximo, vencimiento));
        if (lote == null || lote.reclamaciones().isEmpty()) {
            return new ResultadoLotePush(0, false);
        }
        registrarReclamaciones(lote);
        // La llamada externa y la escritura de resultados ocurren después del commit de la
        // reclamación: si el worker cae aquí, la fila se recupera al vencer su plazo.
        for (ReclamacionPush reclamacion : lote.reclamaciones()) {
            procesador.procesar(reclamacion);
        }
        // El tamaño de la cola se instrumenta una sola vez por lote con trabajo; con la
        // cola vacía no se consulta, así la métrica no introduce sondeo en reposo.
        metricas.pendientes(reclamaciones.pendientes());
        return new ResultadoLotePush(lote.procesados(), lote.quedanElegibles());
    }

    private void registrarReclamaciones(LoteReclamadoPush lote) {
        int preparaciones = (int) lote.reclamaciones().stream()
                .filter(reclamacion -> reclamacion.tipo() == TipoReclamacionPush.PREPARACION)
                .count();
        int envios = lote.procesados() - preparaciones;
        metricas.reclamacion(TipoReclamacionPush.PREPARACION, preparaciones);
        metricas.reclamacion(TipoReclamacionPush.ENVIO, envios);
    }

    @Override
    public Optional<Instant> proximoPlazo() {
        return reclamaciones.proximoPlazo();
    }
}
