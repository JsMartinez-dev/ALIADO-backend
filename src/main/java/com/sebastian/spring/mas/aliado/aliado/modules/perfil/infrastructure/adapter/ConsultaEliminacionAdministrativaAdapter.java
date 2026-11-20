package com.sebastian.spring.mas.aliado.aliado.modules.perfil.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.port.ConsultaEliminacionCuentaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.infrastructure.persistence.jpa.SolicitudEliminacionJpaRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ConsultaEliminacionAdministrativaAdapter implements ConsultaEliminacionCuentaPort {
    private final SolicitudEliminacionJpaRepository solicitudes;

    public ConsultaEliminacionAdministrativaAdapter(SolicitudEliminacionJpaRepository solicitudes) {
        this.solicitudes = solicitudes;
    }

    @Override
    public boolean tieneEliminacionConfirmada(UUID usuarioId) {
        return solicitudes.existsByUsuarioIdAndEstado(usuarioId, "CONFIRMADA");
    }
}
