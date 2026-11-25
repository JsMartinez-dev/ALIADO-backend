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
    private final ConsultaDescubrimientoPort consultaDescubrimientoPort;
    private final ProyeccionPrestadorPublico proyeccionPrestadorPublico;

    public BuscarPrestadoresService(ConsultaDescubrimientoPort consultaDescubrimientoPort,
            ProyeccionPrestadorPublico proyeccionPrestadorPublico) {
        this.consultaDescubrimientoPort = consultaDescubrimientoPort;
        this.proyeccionPrestadorPublico = proyeccionPrestadorPublico;
    }

    @Override
    public BusquedaPrestadoresResult ejecutar(BuscarPrestadoresQuery query) {
        UUID categoriaBuscadaId = null;
        UUID zonaBuscadaId = null;
        if (query.categoria() != null && !query.categoria().isBlank()) {
            categoriaBuscadaId = resolverCategoriaId(query.categoria());
            if (categoriaBuscadaId == null) {
                return new BusquedaPrestadoresResult(List.of(),
                        consultaDescubrimientoPort.categoriasActivas().stream()
                                .map(categoria -> categoria.nombre()).toList());
            }
        }
        if (query.zona() != null && !query.zona().isBlank()) {
            zonaBuscadaId = resolverZonaId(query.zona());
            if (zonaBuscadaId == null) {
                return new BusquedaPrestadoresResult(List.of(),
                        consultaDescubrimientoPort.zonasActivas().stream()
                                .map(zona -> zona.nombre()).toList());
            }
        }
        UUID zonaBuscadaIdFinal = zonaBuscadaId;
        return new BusquedaPrestadoresResult(
                consultaDescubrimientoPort.visiblesPorCategoriaYZona(categoriaBuscadaId, zonaBuscadaId).stream()
                        .filter(perfil -> visibleEnBusqueda(perfil, zonaBuscadaIdFinal))
                        .map(proyeccionPrestadorPublico::resultado).toList(),
                List.of());
    }

    private boolean visibleEnBusqueda(PerfilProfesional perfil, UUID zonaBuscadaId) {
        Visibilidad preferenciaVisibilidad = consultaDescubrimientoPort.preferenciaDe(perfil.prestadorId());
        return preferenciaVisibilidad != Visibilidad.PRIVADO
                && (preferenciaVisibilidad != Visibilidad.SOLO_ZONA || zonaBuscadaId != null);
    }

    private UUID resolverCategoriaId(String categoria) {
        UUID categoriaId = comoUuid(categoria);
        if (categoriaId != null) {
            return consultaDescubrimientoPort.categoriaPorId(categoriaId).isPresent() ? categoriaId : null;
        }
        return consultaDescubrimientoPort.categoriaPorNombre(categoria.trim())
                .map(categoriaEncontrada -> categoriaEncontrada.id()).orElse(null);
    }

    private UUID resolverZonaId(String zona) {
        UUID zonaId = comoUuid(zona);
        if (zonaId != null) {
            return consultaDescubrimientoPort.zonaPorId(zonaId).isPresent() ? zonaId : null;
        }
        return consultaDescubrimientoPort.zonaPorNombre(zona.trim())
                .map(zonaEncontrada -> zonaEncontrada.id()).orElse(null);
    }

    private UUID comoUuid(String valor) {
        try {
            return UUID.fromString(valor.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

}
