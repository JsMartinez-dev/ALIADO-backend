package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.EvaluarCompatibilidadPrestadorQuery;

/**
 * Evaluación individual de compatibilidad reutilizable por otros módulos (F09-PUSH-08).
 *
 * <p>No es un listado ni un matching global: comprueba si una solicitud sigue publicada y
 * si el perfil del prestador sigue siendo compatible, con la misma regla que usa el
 * tablero de oportunidades.</p>
 */
public interface EvaluarCompatibilidadPrestadorUseCase {

    boolean ejecutar(EvaluarCompatibilidadPrestadorQuery query);
}
