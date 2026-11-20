package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port;

import java.util.List;
import java.util.UUID;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.ServicioContratado;

public interface HistorialServiciosPort {
    List<ServicioContratado> porUsuario(UUID usuarioId);
    List<ServicioContratado> porUsuario(UUID usuarioId, int pagina, int tamano);
}
