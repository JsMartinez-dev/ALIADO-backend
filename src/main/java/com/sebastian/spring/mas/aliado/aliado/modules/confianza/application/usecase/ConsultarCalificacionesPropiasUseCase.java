package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.CalificacionesPropiasPageResult;
import java.util.UUID;

public interface ConsultarCalificacionesPropiasUseCase {
    CalificacionesPropiasPageResult ejecutar(UUID clienteId, int pagina, int tamano);
}
