package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoOportunidadAviso;
import java.util.Optional;
import java.util.UUID;

/**
 * Estado actual de la oportunidad enlazada por un aviso (F09-PUSH-10).
 *
 * <p>El puerto lo define notificaciones. Vacío significa que la solicitud no es visible
 * para ese Prestador y el llamador debe responder el error genérico, sin distinguir entre
 * inexistente, borrador o sin acceso.</p>
 */
public interface EstadoOportunidadAvisoPort {

    Optional<EstadoOportunidadAviso> estado(UUID solicitudId, UUID prestadorId);
}
