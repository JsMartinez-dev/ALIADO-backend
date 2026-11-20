package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.HistorialServiciosPort;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.ServicioLecturaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.ServicioContratado;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DatosHistorialAdapter implements HistorialServiciosPort {
    private final ServicioLecturaPort servicios;

    public DatosHistorialAdapter(ServicioLecturaPort servicios) {
        this.servicios = servicios;
    }

    @Override
    public List<ServicioContratado> porUsuario(UUID usuarioId) {
        return servicios.porUsuario(usuarioId);
    }

    @Override
    public List<ServicioContratado> porUsuario(UUID usuarioId, int pagina, int tamano) {
        return servicios.porUsuario(usuarioId, pagina, tamano);
    }
}
