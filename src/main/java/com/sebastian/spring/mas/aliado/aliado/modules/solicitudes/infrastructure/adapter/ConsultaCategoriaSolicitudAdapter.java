package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.EstadoCatalogo;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.ConsultaCategoriaSolicitudPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ConsultaCategoriaSolicitudAdapter implements ConsultaCategoriaSolicitudPort {
    private final CatalogoConsultaPort catalogo;

    public ConsultaCategoriaSolicitudAdapter(CatalogoConsultaPort catalogo) { this.catalogo = catalogo; }

    @Override
    public boolean estaActiva(UUID categoriaId) {
        return catalogo.categoriaPorId(categoriaId).filter(c -> c.estado() == EstadoCatalogo.ACTIVO).isPresent();
    }
}
