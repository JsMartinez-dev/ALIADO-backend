package com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.presentation.mapper;

import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.dto.BuscarPrestadoresQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.dto.BusquedaPrestadoresResult;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.dto.PrestadorPublicoResult;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.presentation.dto.DescubrimientoDtos;

public final class DescubrimientoPresentationMapper {
    private DescubrimientoPresentationMapper() {}

    public static BuscarPrestadoresQuery aQuery(String categoria, String zona) {
        return new BuscarPrestadoresQuery(categoria, zona);
    }

    public static DescubrimientoDtos.BusquedaResponse aResponse(BusquedaPrestadoresResult resultado) {
        return new DescubrimientoDtos.BusquedaResponse(
                resultado.prestadores().stream().map(DescubrimientoPresentationMapper::aResponse).toList(),
                resultado.sugerencias());
    }

    public static DescubrimientoDtos.PrestadorPublicoResponse aResponse(PrestadorPublicoResult perfil) {
        return new DescubrimientoDtos.PrestadorPublicoResponse(perfil.id(), perfil.nombreVisible(), perfil.categorias(), perfil.zonas(),
                perfil.reputacionAgregada(), perfil.cantidadCalificaciones(), perfil.nivelVerificacion(),
                perfil.disponibilidad().stream().map(b -> new DescubrimientoDtos.BloqueResponse(
                        b.diaSemana(), b.horaInicio(), b.horaFin())).toList(), perfil.datosGenerales(),
                perfil.resenas().stream().map(r -> new DescubrimientoDtos.ResenaPublicaResponse(
                        r.id(), r.valor(), r.texto(), r.fechaCreacion())).toList());
    }
}

