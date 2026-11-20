package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.ConsultarTableroQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.OportunidadResult;
import java.util.List;
public interface ConsultarTableroOportunidadesUseCase {List<OportunidadResult> ejecutar(ConsultarTableroQuery query);}
