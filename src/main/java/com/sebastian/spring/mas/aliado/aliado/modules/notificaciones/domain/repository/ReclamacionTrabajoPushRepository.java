package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.DestinoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.LoteReclamadoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.PendientesPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TrabajoPush;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de la reclamación del worker push (F09-PUSH-07, V9).
 *
 * <p>La selección y la actualización de padres y entregas elegibles usan
 * {@code FOR UPDATE SKIP LOCKED} y el reloj de PostgreSQL. {@link #reclamar(int, Duration)}
 * escribe el identificador y el vencimiento de cada fila y emite
 * {@code pg_notify('aliado_notificaciones_trabajo', '')} dentro de la misma transacción
 * corta: la señal solo se entrega si la reclamación se confirma. Debe invocarse dentro de
 * una transacción; el adaptador de infraestructura abre una propia con
 * {@code REQUIRES_NEW} para que el commit ocurra antes de procesar.</p>
 *
 * <p>{@link #confirmarPreparacion(UUID, UUID)} y
 * {@link #guardarResultadoEntrega(ResultadoEntregaPush)} condicionan la escritura al
 * identificador de reclamación vigente y a su vencimiento. Si la reclamación venció o fue
 * reemplazada, actualizan cero filas y el resultado tardío se descarta. El resultado de
 * envío se condiciona además a la generación del token reclamada. Ambas operaciones
 * emiten la señal en la transacción del caso de uso que las invoca cuando cambian una
 * fila, para que otras instancias recalculen su temporizador.</p>
 */
public interface ReclamacionTrabajoPushRepository {

    /**
     * Reclama un lote de hasta {@code maximo} filas entre padres
     * {@code PENDIENTE_PREPARACION} y entregas {@code PENDIENTE} elegibles, cada una con su
     * propio identificador y vencimiento. La mitad del lote se reserva a cada cola y el
     * cupo que sobra de una se ofrece a la otra. Emite una sola señal si reclamó algo.
     *
     * @param maximo      tope total de filas reclamadas; debe ser positivo
     * @param vencimiento vigencia de la reclamación; debe ser positiva
     */
    LoteReclamadoPush reclamar(int maximo, Duration vencimiento);

    /**
     * Marca el padre como {@code PREPARADO} solo si la reclamación indicada sigue vigente.
     * Las entregas asociadas las crea el caso de uso de preparación en la misma
     * transacción.
     *
     * @return {@code true} si la fila cambió; {@code false} si la reclamación venció o fue
     *         reemplazada
     */
    boolean confirmarPreparacion(UUID trabajoId, UUID reclamacionId);

    /**
     * Guarda el resultado de una entrega solo si la reclamación indicada sigue vigente, la
     * entrega continúa {@code PENDIENTE}, la generación coincide con la reclamada y el
     * padre no está finalizado.
     *
     * @return {@code true} si la fila cambió; {@code false} si el resultado llegó tarde o
     *         la entrega ya era terminal
     */
    boolean guardarResultadoEntrega(ResultadoEntregaPush resultado);

    /**
     * Libera la reclamación de una entrega sin registrar resultado, para que vuelva a ser
     * reclamable de inmediato cuando el tiempo restante no alcanza para enviar y guardar el
     * resultado (D-08). La liberación se condiciona al identificador de la reclamación: si
     * la fila ya fue reclamada por otra instancia, no cambia nada. Emite la señal si
     * cambia la fila.
     *
     * @return {@code true} si la reclamación se liberó
     */
    boolean liberarReclamacionDeEntrega(UUID entregaId, UUID reclamacionId);

    /**
     * Menor plazo persistido que puede cambiar la elegibilidad: próximo intento de una
     * entrega con padre {@code PREPARADO}, vencimiento de una reclamación de padre o
     * entrega, o expiración de un trabajo no finalizado. Vacío si no hay ningún plazo.
     */
    Optional<Instant> proximoPlazo();

    /**
     * Tamaño y antigüedad observados de la cola para la instrumentación del cierre de
     * lote (F09-PUSH-11). Es una lectura puntual: el worker solo la usa cuando acaba de
     * procesar trabajo, nunca con la cola en reposo.
     */
    PendientesPush pendientes();

    /** Trabajo padre por identificador. */
    Optional<TrabajoPush> buscarTrabajo(UUID trabajoId);

    /** Entrega por dispositivo por identificador. */
    Optional<EntregaPush> buscarEntrega(UUID entregaId);

    /**
     * Indica si el trabajo alcanzó su vigencia según la hora de PostgreSQL. La elegibilidad
     * de la fila ya la decide el SQL de reclamación; este método evita que la decisión de
     * expirar dependa del reloj del proceso.
     */
    boolean trabajoExpirado(UUID trabajoId);

    /**
     * Crea una entrega {@code PENDIENTE} por destino con el próximo intento en el instante
     * actual de PostgreSQL. La unicidad de V9 ({@code trabajo + instalación + generación})
     * hace idempotente un reintento de la preparación. Debe ejecutarse dentro de la
     * transacción de preparación.
     */
    void crearEntregas(UUID trabajoId, List<DestinoPush> destinos);

    /**
     * Cierra el padre con su motivo solo si la reclamación de preparación sigue vigente y
     * el trabajo continúa pendiente de preparación. Emite la señal si cambia la fila.
     *
     * @return {@code true} si el trabajo se cerró
     */
    boolean finalizarTrabajo(UUID trabajoId, UUID reclamacionId, String motivo);

    /**
     * Cierra el padre {@code PREPARADO} cuando ya no queda ninguna entrega
     * {@code PENDIENTE}. Bloquea la fila del padre para serializar resultados concurrentes
     * y debe invocarse dentro de la transacción que guardó el último resultado.
     *
     * @return {@code true} si esta llamada finalizó el trabajo
     */
    boolean cerrarPadreSiCorresponde(UUID trabajoId);
}
