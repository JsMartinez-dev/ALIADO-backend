package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.dto.PropuestaResult;
import java.util.List;
import java.util.UUID;

public interface ListarPropuestasPropiasUseCase {
    List<PropuestaResult> ejecutar(UUID prestadorId, int pagina, int tamano);
}
