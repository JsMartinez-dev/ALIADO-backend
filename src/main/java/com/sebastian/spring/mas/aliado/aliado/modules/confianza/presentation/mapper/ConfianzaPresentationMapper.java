package com.sebastian.spring.mas.aliado.aliado.modules.confianza.presentation.mapper;

import java.util.UUID;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.presentation.dto.ConfianzaDtos;

public final class ConfianzaPresentationMapper {
    private ConfianzaPresentationMapper() {}
    public static CalificarServicioCommand aCommand(UUID servicioId, UUID clienteId, ConfianzaDtos.CalificarRequest req) {
        return new CalificarServicioCommand(servicioId, clienteId, req.valor());
    }
    public static AgregarResenaCommand aCommand(UUID calificacionId, UUID clienteId, ConfianzaDtos.ResenaRequest req) {
        return new AgregarResenaCommand(calificacionId, clienteId, req.texto());
    }
    public static ReportarResenaCommand aCommand(UUID resenaId, UUID reportanteId, ConfianzaDtos.ReporteRequest req) {
        return new ReportarResenaCommand(resenaId, reportanteId, req.motivo());
    }
    public static ConfianzaDtos.CalificacionResponse aResponse(CalificacionResult result) {
        return new ConfianzaDtos.CalificacionResponse(result.id(), result.reputacionActualizada());
    }
    public static ConfianzaDtos.ConfianzaResponse aResponse(ConfianzaResult result) {
        return new ConfianzaDtos.ConfianzaResponse(result.reputacion(), result.cantidadCalificaciones(), result.nivelVerificacion());
    }
}
