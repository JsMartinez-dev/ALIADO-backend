package com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.port.ConsultaDescubrimientoPort;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.dto.PrestadorPublicoResult;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.domain.model.PrestadorPublico;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import java.util.List;
public class ProyeccionPrestadorPublico {

    private final ConsultaDescubrimientoPort consultas;

    public ProyeccionPrestadorPublico(ConsultaDescubrimientoPort consultas) {
        this.consultas = consultas;
    }

    public PrestadorPublicoResult resultado(PerfilProfesional perfil) {
        PrestadorPublico publico = proyectar(perfil);
        return new PrestadorPublicoResult(publico.id(), publico.categorias(), publico.zonas(),
                publico.reputacionAgregada(), publico.cantidadCalificaciones(), publico.nivelVerificacion(),
                publico.disponibilidad().stream().map(b -> new PrestadorPublicoResult.Bloque(
                        b.diaSemana(), b.horaInicio(), b.horaFin())).toList(),
                publico.datosGenerales(), publico.resenas().stream().map(r -> new PrestadorPublicoResult.Resena(
                        r.id(), r.valor(), r.texto(), r.fechaCreacion())).toList());
    }

    public PrestadorPublico proyectar(PerfilProfesional perfil) {
        List<String> categorias = perfil.categoriaIds().stream()
                .map(id -> consultas.categoriaPorId(id).map(c -> c.nombre()).orElse(id.toString())).toList();
        List<String> zonas = perfil.zonaIds().stream()
                .map(id -> consultas.zonaPorId(id).map(z -> z.nombre()).orElse(id.toString())).toList();
        var confianzaInfo = consultas.confianzaDe(perfil.prestadorId());
        return new PrestadorPublico(perfil.prestadorId(), categorias, zonas,
                confianzaInfo.reputacion().valor(), confianzaInfo.reputacion().cantidad(),
                confianzaInfo.nivelVerificacion().name(),
                perfil.disponibilidades().stream()
                        .map(d -> new PrestadorPublico.Bloque(d.diaSemana(), d.horaInicio(), d.horaFin())).toList(),
                PerfilProfesional.datosPublicos(perfil.datosGenerales()),
                consultas.resenasVisiblesDe(perfil.prestadorId()).stream()
                        .map(r -> new PrestadorPublico.ResenaPublica(r.id(), r.valor(), r.texto(),
                                r.fechaCreacion())).toList());
    }
}

