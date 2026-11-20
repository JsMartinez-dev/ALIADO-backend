package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.usecase;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.dto.CrearPropuestaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.dto.PropuestaResult;
public interface CrearPropuestaUseCase { PropuestaResult ejecutar(CrearPropuestaCommand command); }
