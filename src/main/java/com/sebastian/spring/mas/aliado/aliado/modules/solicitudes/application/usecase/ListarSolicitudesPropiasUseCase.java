package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase;

import java.util.UUID;
import java.util.List;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;
public interface ListarSolicitudesPropiasUseCase {
    List<SolicitudResult> ejecutar(UUID clienteId);
}
