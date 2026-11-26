package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SolicitudConsultaPort {
    Optional<Solicitud> porId(UUID id);
    List<Solicitud> publicadas();
    void marcarContratada(UUID solicitudId);
    List<Solicitud> buscarPorTexto(String texto);
}
