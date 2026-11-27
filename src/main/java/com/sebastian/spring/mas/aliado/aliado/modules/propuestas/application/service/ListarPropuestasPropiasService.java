package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.usecase.ListarPropuestasPropiasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.dto.PropuestaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.repository.PropuestaRepository;
import java.util.List;
import java.util.UUID;

public class ListarPropuestasPropiasService implements ListarPropuestasPropiasUseCase {
    private final PropuestaRepository propuestas;

    public ListarPropuestasPropiasService(PropuestaRepository propuestas) {
        this.propuestas = propuestas;
    }

    @Override
    public List<PropuestaResult> ejecutar(UUID prestadorId, int pagina, int tamano) {
        if (pagina < 0 || tamano < 1 || tamano > 100 || (long) pagina * tamano > Integer.MAX_VALUE) {
            throw new BusinessException.BadRequest("PAGINACION_INVALIDA", "Página o tamaño fuera de rango.");
        }
        return propuestas.porPrestador(prestadorId, pagina, tamano).stream().map(PropuestaResult::desde).toList();
    }
}
