package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.CalificacionesPropiasPageResult;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase.ConsultarCalificacionesPropiasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.CalificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.ResenaRepository;
import java.util.UUID;

public class ConsultarCalificacionesPropiasService implements ConsultarCalificacionesPropiasUseCase {
    private final CalificacionRepository calificaciones;
    private final ResenaRepository resenas;

    public ConsultarCalificacionesPropiasService(CalificacionRepository calificaciones, ResenaRepository resenas) {
        this.calificaciones = calificaciones;
        this.resenas = resenas;
    }

    @Override
    public CalificacionesPropiasPageResult ejecutar(UUID clienteId, int pagina, int tamano) {
        var todas = calificaciones.porClienteId(clienteId);
        int total = todas.size();
        int totalPaginas = total == 0 ? 0 : (total + tamano - 1) / tamano;
        int desde = Math.min(pagina * tamano, total);
        int hasta = Math.min(desde + tamano, total);
        var items = todas.subList(desde, hasta).stream().map(c -> {
            var resena = resenas.porCalificacionId(c.id());
            return new CalificacionesPropiasPageResult.Item(c.id(), c.servicioId(), c.valor(), c.fecha(),
                    resena.map(r -> new CalificacionesPropiasPageResult.Resena(
                            r.id(), r.texto(), r.fechaCreacion())).orElse(null));
        }).toList();
        return new CalificacionesPropiasPageResult(items, pagina, tamano, total, totalPaginas);
    }
}
