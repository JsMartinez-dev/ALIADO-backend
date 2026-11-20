package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.ConsultarSolicitudPropiaQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.SolicitudNoEncontradaException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.ConsultarSolicitudPropiaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.repository.SolicitudRepository;
public class ConsultarSolicitudPropiaService implements ConsultarSolicitudPropiaUseCase {
    private final SolicitudRepository solicitudes;
    public ConsultarSolicitudPropiaService(SolicitudRepository solicitudes) { this.solicitudes = solicitudes; }
    @Override
    public SolicitudResult ejecutar(ConsultarSolicitudPropiaQuery query) {
        return solicitudes.porId(query.solicitudId()).filter(s -> s.clienteId().equals(query.clienteId()))
                .map(SolicitudResult::desde).orElseThrow(SolicitudNoEncontradaException::new);
    }
}
