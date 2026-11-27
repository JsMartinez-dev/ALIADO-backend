package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RegistrarResultadoEntregaPushCommand;

/**
 * Aplica el resultado del proveedor a una entrega y evalúa el cierre del padre
 * (F09-PUSH-08).
 */
public interface RegistrarResultadoEntregaPushUseCase {

    void ejecutar(RegistrarResultadoEntregaPushCommand command);
}
