package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.domain.model;

import java.util.Set;
import java.util.UUID;

public record CriteriosCompatibilidad(boolean solicitudPublicada, boolean perfilPublico,
                                     boolean perfilPrivado, UUID categoriaSolicitud, UUID zonaSolicitud,
                                     Set<UUID> categoriasPrestador, Set<UUID> zonasPrestador,
                                     boolean disponibilidadDeclarada) {
    public boolean esCompatible() {
        return solicitudPublicada && perfilPublico && !perfilPrivado
                && categoriasPrestador.contains(categoriaSolicitud)
                && zonasPrestador.contains(zonaSolicitud)
                && disponibilidadDeclarada;
    }
}
