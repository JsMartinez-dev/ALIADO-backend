package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.EnvioPushPendiente;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.IniciarEntregaPushCommand;
import java.util.Optional;

/**
 * Revalida una entrega reclamada antes de llamar al proveedor (F09-PUSH-08).
 *
 * <p>Devuelve el envío listo para despachar fuera de la transacción. Si la oportunidad,
 * la preferencia o el vínculo del token dejaron de ser válidos, persiste la supresión y
 * devuelve vacío sin contactar al proveedor.</p>
 */
public interface IniciarEntregaPushUseCase {

    Optional<EnvioPushPendiente> ejecutar(IniciarEntregaPushCommand command);
}
