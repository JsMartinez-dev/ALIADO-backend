package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.FiltrarTableroCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import java.util.List;
public interface FiltrarTableroOportunidadesUseCase {
    List<Solicitud> ejecutar(FiltrarTableroCommand command);
}
