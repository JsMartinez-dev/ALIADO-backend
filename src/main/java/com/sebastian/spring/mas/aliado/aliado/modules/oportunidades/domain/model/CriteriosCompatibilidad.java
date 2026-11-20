package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.domain.model;

import java.util.Set;
import java.util.UUID;

/** Datos del cálculo vigente. D-03 mantiene pendiente la comparación de ventanas horarias. */
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
