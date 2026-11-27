package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.OportunidadDesdeAvisoResult;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResolverOportunidadAvisoCommand;

/**
 * Resuelve el estado permitido al destinatario de un aviso de oportunidad
 * (F09-PUSH-10).
 *
 * <p>Solo el destinatario y un aviso {@code NUEVA_OPORTUNIDAD} autorizan la consulta. Un
 * aviso ajeno, inexistente o de otro tipo comparte el mismo error genérico.</p>
 */
public interface ResolverOportunidadAvisoUseCase {

    OportunidadDesdeAvisoResult ejecutar(ResolverOportunidadAvisoCommand command);
}
