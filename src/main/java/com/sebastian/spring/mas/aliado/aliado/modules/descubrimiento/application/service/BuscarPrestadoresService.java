package com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.dto.BuscarPrestadoresQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.dto.BusquedaPrestadoresResult;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.port.ConsultaDescubrimientoPort;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.usecase.BuscarPrestadoresUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Visibilidad;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import java.util.List;
import java.util.UUID;

public class BuscarPrestadoresService implements BuscarPrestadoresUseCase {
    private final ConsultaDescubrimientoPort consultas;
    private final ProyeccionPrestadorPublico proyeccion;

    public BuscarPrestadoresService(ConsultaDescubrimientoPort consultas, ProyeccionPrestadorPublico proyeccion) {
        this.consultas = consultas;
        this.proyeccion = proyeccion;
    }

    @Override
    public BusquedaPrestadoresResult ejecutar(BuscarPrestadoresQuery query) {
        UUID categoriaId = null;
        UUID zonaId = null;
        if (query.categoria() != null && !query.categoria().isBlank()) {
            categoriaId = resolverCategoriaId(query.categoria());
            if (categoriaId == null) {
                return new BusquedaPrestadoresResult(List.of(),
                        consultas.categoriasActivas().stream().map(c -> c.nombre()).toList()); //Devulve una sugerencia de categorias activas
            }
        }
        if (query.zona() != null && !query.zona().isBlank()) {
            zonaId = resolverZonaId(query.zona());
            if (zonaId == null) {
                return new BusquedaPrestadoresResult(List.of(),
                        consultas.zonasActivas().stream().map(z -> z.nombre()).toList());
            }
        }
        UUID zonaBuscada = zonaId;
        return new BusquedaPrestadoresResult(consultas.visiblesPorCategoriaYZona(categoriaId, zonaId).stream()
                .filter(perfil -> visibleEnBusqueda(perfil, zonaBuscada))
                .map(proyeccion::resultado).toList(), List.of());
    }

    private boolean visibleEnBusqueda(PerfilProfesional perfil, UUID zonaId) {
        Visibilidad preferencia = consultas.preferenciaDe(perfil.prestadorId());
        return preferencia != Visibilidad.PRIVADO && (preferencia != Visibilidad.SOLO_ZONA || zonaId != null);
    }

    private UUID resolverCategoriaId(String param) {
        UUID id = comoUuid(param);
        if (id != null) {
            return consultas.categoriaPorId(id).isPresent() ? id : null;
        }
        return consultas.categoriaPorNombre(param.trim()).map(c -> c.id()).orElse(null);
    }

    private UUID resolverZonaId(String param) {
        UUID id = comoUuid(param);
        if (id != null) {
            return consultas.zonaPorId(id).isPresent() ? id : null;
        }
        return consultas.zonaPorNombre(param.trim()).map(z -> z.id()).orElse(null);
    }

    private UUID comoUuid(String param) {
        try {
            return UUID.fromString(param.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

}

