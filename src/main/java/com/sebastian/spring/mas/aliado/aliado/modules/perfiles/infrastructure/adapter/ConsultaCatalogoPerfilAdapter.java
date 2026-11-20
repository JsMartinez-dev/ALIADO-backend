package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.EstadoCatalogo;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.ConsultaCatalogoPerfilPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ConsultaCatalogoPerfilAdapter implements ConsultaCatalogoPerfilPort {
    private final CatalogoConsultaPort catalogo;

    public ConsultaCatalogoPerfilAdapter(CatalogoConsultaPort catalogo) { this.catalogo = catalogo; }

    @Override
    public boolean categoriaActiva(UUID id) {
        return catalogo.categoriaPorId(id).map(c -> c.estado() == EstadoCatalogo.ACTIVO).orElse(false);
    }

    @Override
    public boolean zonaActiva(UUID id) {
        return catalogo.zonaPorId(id).map(z -> z.estado() == EstadoCatalogo.ACTIVO).orElse(false);
    }
}
