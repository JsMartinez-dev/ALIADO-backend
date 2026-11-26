package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.ResolverEstadoOportunidadQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.domain.model.EstadoOportunidad;
import java.util.Optional;

/**
 * Resuelve el estado mínimo que puede observar un Prestador sobre una solicitud
 * (F09-PUSH-10).
 *
 * <p>Reutiliza la misma regla que el detalle: {@code ABIERTA} solo con la solicitud
 * publicada y acceso vigente. Una solicitud cerrada devuelve {@code CERRADA} porque el
 * aviso propio del destinatario autoriza ese estado mínimo. Vacío significa que la
 * oportunidad no es visible y el borde HTTP debe conservar el error genérico.</p>
 */
public interface ResolverEstadoOportunidadUseCase {

    Optional<EstadoOportunidad> ejecutar(ResolverEstadoOportunidadQuery query);
}
