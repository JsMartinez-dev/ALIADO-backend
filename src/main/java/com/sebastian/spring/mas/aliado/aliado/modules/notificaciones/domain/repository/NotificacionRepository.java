package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoLectura;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificacionRepository {
    boolean existeEvento(String tipo, UUID aggregateId, UUID destinatarioId, int eventVersion);
    List<Notificacion> porDestinatario(UUID destinatarioId);
    List<Notificacion> porDestinatarioYTipo(UUID destinatarioId, String tipo);

    /** Aviso por identificador, sin exponer la entidad de persistencia. */
    Optional<Notificacion> porId(UUID id);
    Notificacion guardar(Notificacion notificacion);

    /**
     * Inserta el aviso si el evento (tipo, agregado, destinatario y versión) no existe.
     * La unicidad de V4 resuelve la concurrencia sin lanzar un error que invalide la
     * transacción de negocio.
     *
     * @return {@code true} solo si esta llamada creó la fila.
     */
    boolean insertarSiNoExiste(Notificacion notificacion);

    /**
     * Registra la primera lectura de un aviso propio (F09-BANDEJA-02). Filtra por
     * identificador y destinatario y conserva la fecha ya guardada, de modo que un
     * reintento o una escritura concurrente no sustituyen la primera lectura.
     *
     * @return {@code true} si el aviso pertenece al destinatario, estuviera ya leído o
     *         pendiente; {@code false} si no existe o pertenece a otra cuenta, sin
     *         distinguir ambos casos.
     */
    boolean marcarLeida(UUID id, UUID destinatarioId, OffsetDateTime leidaEn);

    /**
     * Marca como leídas todas las pendientes de un destinatario en una sola sentencia
     * (F09-BANDEJA-02).
     *
     * @return número de filas actualizadas; {@code 0} si la cuenta no tenía pendientes.
     */
    int marcarTodasLeidas(UUID destinatarioId, OffsetDateTime leidaEn);

    /**
     * Página de la bandeja propia (F09-BANDEJA-03), ordenada por
     * {@code fecha_generacion DESC, id DESC} y filtrada en la consulta por destinatario,
     * estado de lectura y tipo.
     *
     * @param antesDeFecha fecha del cursor; {@code null} junto con {@code antesDeId} inicia
     *                     la paginación.
     * @param antesDeId    identificador del cursor, desempate del par fecha–ID.
     * @param limite       máximo de filas a devolver; el caso de uso pide {@code limite + 1}
     *                     para detectar otra página.
     * @return como máximo {@code limite} avisos posteriores al cursor.
     */
    List<Notificacion> buscarBandeja(UUID destinatarioId, EstadoLectura estadoLectura,
                                     TipoNotificacion tipo, OffsetDateTime antesDeFecha,
                                     UUID antesDeId, int limite);

    /** {@code true} si la cuenta tiene al menos un aviso con {@code leida_en IS NULL}. */
    boolean hayNoLeidas(UUID destinatarioId);
}
