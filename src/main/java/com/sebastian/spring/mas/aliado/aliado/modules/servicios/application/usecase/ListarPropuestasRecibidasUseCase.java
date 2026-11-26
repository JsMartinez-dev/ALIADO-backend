package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.usecase;

import java.util.List;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto.*;

public interface ListarPropuestasRecibidasUseCase {
    List<com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.dto.PropuestaResult> ejecutar(ListadoPropuestasQuery command);
}
