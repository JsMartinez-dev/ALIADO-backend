package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ConsultarIndicadoresPrestadorUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.IndicadoresResult;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.HistorialServiciosPort;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.IndicadoresPrestadorPort;
import java.util.UUID;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.EstadoServicio;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.exception.PreferenciasOperacionException;

public class ConsultarIndicadoresPrestadorService implements ConsultarIndicadoresPrestadorUseCase {
    private final HistorialServiciosPort servicios;
    private final IndicadoresPrestadorPort indicadores;

    public ConsultarIndicadoresPrestadorService(HistorialServiciosPort servicios, IndicadoresPrestadorPort indicadores) {
        this.servicios = servicios;
        this.indicadores = indicadores;
    }

    @Override
    public IndicadoresResult ejecutar(UUID prestadorId) {
        if (!indicadores.esPrestador(prestadorId)) {
            throw new PreferenciasOperacionException(PreferenciasOperacionException.Tipo.PROHIBIDO, "SOLO_PRESTADOR",
                    "Los indicadores son solo para prestadores.");
        }
        var todos = servicios.porUsuario(prestadorId);
        long finalizados = todos.stream()
                .filter(s -> s.estado() == EstadoServicio.FINALIZADO || s.estado() == EstadoServicio.TERMINADO)
                .count();
        long enCurso = todos.stream()
                .filter(s -> s.estado() == EstadoServicio.EN_EJECUCION || s.estado() == EstadoServicio.CONTRATADO)
                .count();
        var reputacion = indicadores.reputacionDe(prestadorId);
        return new IndicadoresResult(todos.size(), enCurso, finalizados, reputacion.valor(), reputacion.cantidad());
    }
}
