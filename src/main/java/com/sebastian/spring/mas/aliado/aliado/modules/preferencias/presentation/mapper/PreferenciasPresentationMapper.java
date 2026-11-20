package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.presentation.mapper;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.ActualizarPreferenciaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.ActividadResult;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.DescargaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.IndicadoresResult;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.PreferenciaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.ServicioResult;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.presentation.dto.PreferenciasDtos;
import java.util.UUID;

public final class PreferenciasPresentationMapper {
    private PreferenciasPresentationMapper() {}

    public static ActualizarPreferenciaCommand aCommand(UUID usuarioId, PreferenciasDtos.ActualizarPreferenciaRequest req) {
        return new ActualizarPreferenciaCommand(usuarioId, req.tipoAviso(), req.canal(), req.habilitada());
    }

    public static PreferenciasDtos.PreferenciaResponse aResponse(PreferenciaResult r) {
        return new PreferenciasDtos.PreferenciaResponse(r.tipoAviso(), r.canal(), r.habilitada(), r.configurable());
    }

    public static PreferenciasDtos.ServicioResumen aResponse(ServicioResult r) {
        return new PreferenciasDtos.ServicioResumen(r.id(), r.estado(), r.fechaContratacion());
    }

    public static PreferenciasDtos.DescargaResponse aResponse(DescargaResult r) {
        return new PreferenciasDtos.DescargaResponse(r.id(), r.archivoKey(), r.estado());
    }

    public static PreferenciasDtos.IndicadoresResponse aResponse(IndicadoresResult r) {
        return new PreferenciasDtos.IndicadoresResponse(r.totalServicios(), r.enCurso(), r.finalizados(),
                r.reputacion(), r.cantidadCalificaciones());
    }

    public static PreferenciasDtos.ActividadResponse aResponse(ActividadResult r) {
        return new PreferenciasDtos.ActividadResponse(r.tipo(), r.resumen(), r.fecha());
    }
}
