package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.AdjuntarEvidenciaIdempotenteCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.EvidenciaIdempotenteResult;

public interface AdjuntarEvidenciaReporteIdempotenteUseCase {
    EvidenciaIdempotenteResult ejecutar(AdjuntarEvidenciaIdempotenteCommand command);
}
