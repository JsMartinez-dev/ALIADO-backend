package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.PendientesPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoReclamacionPush;
import java.time.Duration;

/**
 * Instrumentación del ciclo push (F09-PUSH-11).
 *
 * <p>Distingue encolado, reclamación, reintento, aceptación y fallo. Las métricas se
 * alimentan solo con los eventos que produce el worker y el encolado transaccional: no
 * existe ningún sondeo periódico de la cola. {@code pendientes} se registra únicamente al
 * terminar un lote con trabajo, nunca con la cola en reposo.</p>
 *
 * <p>Los identificadores son técnicos: tipo, categoría de reclamación, código corto de
 * error y duraciones. Nunca se registran tokens, identificadores de instalación ni
 * contenido del aviso.</p>
 */
public interface MetricasPushPort {

    /** Un trabajo push quedó encolado en la transacción de publicación. */
    void encolado(TipoNotificacion tipo);

    /** El worker reclamó {@code cantidad} filas de la categoría indicada. */
    void reclamacion(TipoReclamacionPush tipo, int cantidad);

    /** Una entrega conserva su estado pendiente y queda reprogramada. */
    void reintento(TipoNotificacion tipo, int intentos);

    /** El proveedor aceptó el envío; la latencia va del encolado a la aceptación. */
    void aceptacion(TipoNotificacion tipo, Duration latencia);

    /** Resultado terminal no aceptado, supresión o cierre sin envío. */
    void fallo(String codigo);

    /**
     * Tamaño y antigüedad observados de la cola al cerrar un lote. La implementación
     * emite además el resumen del ciclo, que es la superficie de inspección operativa.
     */
    void pendientes(PendientesPush pendientes);
}
