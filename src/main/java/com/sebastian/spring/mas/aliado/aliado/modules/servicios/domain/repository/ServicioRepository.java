package com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.repository;

import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.ServicioContratado;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServicioRepository {
    Optional<ServicioContratado> porId(UUID id);
    Optional<ServicioContratado> porSolicitudId(UUID solicitudId);
    List<ServicioContratado> porUsuario(UUID usuarioId);
    List<ServicioContratado> porUsuario(UUID usuarioId, int pagina, int tamano);
    long contarPorUsuario(UUID usuarioId);
    List<ServicioContratado> activosDeUsuario(UUID usuarioId);
    ServicioContratado guardar(ServicioContratado servicio);
}
