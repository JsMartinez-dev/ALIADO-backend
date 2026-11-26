package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.LoteReclamadoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEntregaPush;
import java.time.Duration;
import java.time.Instant;
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
     * Menor plazo persistido que puede cambiar la elegibilidad: próximo intento de una
     * entrega, vencimiento de una reclamación de padre o entrega, o expiración de un
     * trabajo no finalizado. Vacío si no hay ningún plazo.
     */
    Optional<Instant> proximoPlazo();
}
