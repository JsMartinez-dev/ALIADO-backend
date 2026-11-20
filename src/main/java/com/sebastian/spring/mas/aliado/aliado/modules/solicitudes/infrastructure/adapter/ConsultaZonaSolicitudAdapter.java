package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.EstadoCatalogo;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.ConsultaZonaSolicitudPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adapta el contrato público existente sin acceder a persistencia de catálogo. */
@Component
public class ConsultaZonaSolicitudAdapter implements ConsultaZonaSolicitudPort {
    private final CatalogoConsultaPort catalogo;

    public ConsultaZonaSolicitudAdapter(CatalogoConsultaPort catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public boolean estaActiva(UUID zonaId) {
        return catalogo.zonaPorId(zonaId).filter(z -> z.estado() == EstadoCatalogo.ACTIVO).isPresent();
    }
}
