package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.ResolverEstadoOportunidadQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.ConsultaOportunidadesPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.ResolverEstadoOportunidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.domain.model.EstadoOportunidad;
import java.util.Objects;
import java.util.Optional;

/**
 * Estado mínimo de una oportunidad para el destinatario de un aviso (F09-PUSH-10).
 *
 * <p>La solicitud publicada reutiliza {@link EvaluacionCompatibilidad}, la misma regla del
 * detalle y del tablero. Una solicitud cancelada o contratada devuelve {@code CERRADA}
 * únicamente cuando el llamador ya comprobó que el aviso es propio; un borrador o una
 * solicitud inexistente no revelan nada. La decisión de cotizar sigue perteneciendo a la
 * API de propuestas.</p>
 */
public class ResolverEstadoOportunidadService implements ResolverEstadoOportunidadUseCase {

    private final ConsultaOportunidadesPort consultas;
    private final EvaluacionCompatibilidad evaluacion;

    public ResolverEstadoOportunidadService(ConsultaOportunidadesPort consultas,
                                            EvaluacionCompatibilidad evaluacion) {
        this.consultas = Objects.requireNonNull(consultas);
        this.evaluacion = Objects.requireNonNull(evaluacion);
    }

    @Override
    public Optional<EstadoOportunidad> ejecutar(ResolverEstadoOportunidadQuery query) {
        var solicitud = consultas.solicitudPorId(query.solicitudId()).orElse(null);
        if (solicitud == null) {
            return Optional.empty();
        }
        return switch (solicitud.estado()) {
            case PUBLICADA -> consultas.perfilPorPrestadorId(query.prestadorId())
                    .filter(perfil -> evaluacion.compatible(solicitud, perfil))
                    .map(perfil -> EstadoOportunidad.ABIERTA);
            case CANCELADA, CONTRATADA -> Optional.of(EstadoOportunidad.CERRADA);
            case BORRADOR -> Optional.empty();
        };
    }
}
