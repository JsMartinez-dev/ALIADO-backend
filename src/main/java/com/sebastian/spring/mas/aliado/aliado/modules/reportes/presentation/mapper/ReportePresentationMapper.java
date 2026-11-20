package com.sebastian.spring.mas.aliado.aliado.modules.reportes.presentation.mapper;

import java.util.UUID;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.presentation.dto.ReporteDtos;

public final class ReportePresentationMapper {
    private ReportePresentationMapper() {}
    public static ReportarUsuarioCommand aUsuarioCommand(UUID id, UUID actor, ReporteDtos.CrearRequest req) {
        return new ReportarUsuarioCommand(id, actor, req.motivo());
    }
    public static ReportarServicioCommand aServicioCommand(UUID id, UUID actor, ReporteDtos.CrearRequest req) {
        return new ReportarServicioCommand(id, actor, req.motivo());
    }
    public static ReporteDtos.ReporteResponse aResponse(ReporteResult r) {
        return new ReporteDtos.ReporteResponse(r.id(), r.tipo(), r.objetivoId(), r.motivo(), r.estado(), r.fechaCreacion(),
            r.evidencias().stream().map(e -> new ReporteDtos.EvidenciaResponse(e.id(), e.archivoKey(), e.tipoContenido())).toList());
    }
}
