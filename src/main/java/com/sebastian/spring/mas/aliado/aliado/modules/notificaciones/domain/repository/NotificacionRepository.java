package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificacionRepository {
    boolean existeEvento(String tipo, UUID aggregateId, UUID destinatarioId, int eventVersion);
    List<Notificacion> porDestinatario(UUID destinatarioId);
    List<Notificacion> porDestinatarioYTipo(UUID destinatarioId, String tipo);

    /** Aviso por identificador, sin exponer la entidad de persistencia. */
    Optional<Notificacion> porId(UUID id);
    List<Notificacion> pendientesParaEntrega();
    Notificacion guardar(Notificacion notificacion);

    /**
     * Inserta el aviso si el evento (tipo, agregado, destinatario y versión) no existe.
     * La unicidad de V4 resuelve la concurrencia sin lanzar un error que invalide la
     * transacción de negocio.
     *
     * @return {@code true} solo si esta llamada creó la fila.
     */
    boolean insertarSiNoExiste(Notificacion notificacion);
}
