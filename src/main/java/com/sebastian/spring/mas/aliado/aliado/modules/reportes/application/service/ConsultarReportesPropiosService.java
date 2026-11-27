package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.EvidenciaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.ReporteResult;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.ReportesPropiosPageResult;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.usecase.ConsultarReportesPropiosUseCase;
import java.util.UUID;

public class ConsultarReportesPropiosService implements ConsultarReportesPropiosUseCase {
    private final OperacionesReportes operaciones;

    public ConsultarReportesPropiosService(OperacionesReportes operaciones) {
        this.operaciones = operaciones;
    }

    @Override
    public ReportesPropiosPageResult ejecutar(UUID reportanteId, int pagina, int tamano) {
        var todos = operaciones.reportesDe(reportanteId);
        int total = todos.size();
        int totalPaginas = total == 0 ? 0 : (total + tamano - 1) / tamano;
        int desde = Math.min(pagina * tamano, total);
        int hasta = Math.min(desde + tamano, total);
        var items = todos.subList(desde, hasta).stream()
                .map(r -> new ReporteResult(r.id(), r.tipo().name(), r.objetivoId(), r.motivo(),
                        r.estado().name(), r.fechaCreacion(),
                        operaciones.evidenciasDe(r.id()).stream()
                                .map(e -> new EvidenciaResult(e.id(), e.archivoKey(), e.tipoContenido()))
                                .toList()))
                .toList();
        return new ReportesPropiosPageResult(items, pagina, tamano, total, totalPaginas);
    }
}
