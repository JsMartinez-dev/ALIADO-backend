package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service;

import java.util.UUID;
import java.util.List;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.ListarSolicitudesPropiasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.repository.SolicitudRepository;
public class ListarSolicitudesPropiasService implements ListarSolicitudesPropiasUseCase {

    private final SolicitudRepository solicitudesRepository;

    public ListarSolicitudesPropiasService(SolicitudRepository solicitudesRepository) { this.solicitudesRepository = solicitudesRepository; }
    @Override
    public List<SolicitudResult> ejecutar(UUID clienteId) {
        return solicitudesRepository.porCliente(clienteId).stream().map(SolicitudResult::desde).toList();
    }

}
