package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.ReportesPropiosPageResult;
import java.util.UUID;

public interface ConsultarReportesPropiosUseCase {
    ReportesPropiosPageResult ejecutar(UUID reportanteId, int pagina, int tamano);
}
