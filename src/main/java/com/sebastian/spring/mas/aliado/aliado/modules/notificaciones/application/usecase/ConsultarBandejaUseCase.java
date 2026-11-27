package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.BandejaAvisosResult;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ConsultarBandejaCommand;

/** Consulta paginada de la bandeja propia por estado de lectura y tipo (F09-BANDEJA-03). */
public interface ConsultarBandejaUseCase {
    BandejaAvisosResult ejecutar(ConsultarBandejaCommand command);
}
