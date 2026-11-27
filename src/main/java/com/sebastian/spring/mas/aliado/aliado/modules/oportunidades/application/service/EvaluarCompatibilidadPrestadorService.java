package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.EvaluarCompatibilidadPrestadorQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.ConsultaOportunidadesPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.EvaluarCompatibilidadPrestadorUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import java.util.Objects;
import java.util.Optional;

/**
 * Reutiliza {@link EvaluacionCompatibilidad} para una pareja solicitud/prestador
 * (F09-PUSH-08).
 *
 * <p>La regla es idéntica a la del tablero y el detalle: la solicitud debe seguir
 * publicada, el perfil expuesto públicamente y las categorías, zonas, disponibilidad y
 * privacidad deben seguir siendo compatibles. El matching global de todos los prestadores
 * sigue ocurriendo solo al publicar.</p>
 */
public class EvaluarCompatibilidadPrestadorService implements EvaluarCompatibilidadPrestadorUseCase {

    private final ConsultaOportunidadesPort consultas;
    private final EvaluacionCompatibilidad evaluacion;

    public EvaluarCompatibilidadPrestadorService(ConsultaOportunidadesPort consultas,
                                                 EvaluacionCompatibilidad evaluacion) {
        this.consultas = Objects.requireNonNull(consultas);
        this.evaluacion = Objects.requireNonNull(evaluacion);
    }

    @Override
    public boolean ejecutar(EvaluarCompatibilidadPrestadorQuery query) {
        Optional<Solicitud> solicitud = consultas.solicitudPorId(query.solicitudId());
        Optional<PerfilProfesional> perfil = consultas.perfilPorPrestadorId(query.prestadorId());
        if (solicitud.isEmpty() || perfil.isEmpty()) {
            return false;
        }
        return evaluacion.compatible(solicitud.get(), perfil.get());
    }
}
