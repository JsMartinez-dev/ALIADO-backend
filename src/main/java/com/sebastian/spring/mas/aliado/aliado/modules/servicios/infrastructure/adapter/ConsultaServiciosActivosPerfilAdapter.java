package com.sebastian.spring.mas.aliado.aliado.modules.servicios.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.ConsultaServiciosActivosPort;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.repository.ServicioRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Conserva provisionalmente el criterio de servicios activos vigente hasta resolver la política de eliminación. */
@Component
public class ConsultaServiciosActivosPerfilAdapter implements ConsultaServiciosActivosPort {
    private final ServicioRepository servicios;

    public ConsultaServiciosActivosPerfilAdapter(ServicioRepository servicios) {
        this.servicios = servicios;
    }

    @Override
    public boolean bloqueaEliminacion(UUID usuarioId) {
        return !servicios.activosDeUsuario(usuarioId).isEmpty();
    }
}
