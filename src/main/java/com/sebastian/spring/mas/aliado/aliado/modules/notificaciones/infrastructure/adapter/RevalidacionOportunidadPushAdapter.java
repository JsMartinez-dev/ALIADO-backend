package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.RevalidacionOportunidadPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.EvaluarCompatibilidadPrestadorQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.EvaluarCompatibilidadPrestadorUseCase;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Adaptador de la revalidación de oportunidad para el worker push (F09-PUSH-08).
 *
 * <p>El puerto lo define notificaciones. Este adaptador consume el caso de uso público de
 * oportunidades, que reutiliza {@code EvaluacionCompatibilidad}; así el módulo de
 * notificaciones no depende de entidades, repositorios ni adaptadores de persistencia
 * ajenos.</p>
 */
@Component
public class RevalidacionOportunidadPushAdapter implements RevalidacionOportunidadPushPort {

    private final EvaluarCompatibilidadPrestadorUseCase evaluacion;

    public RevalidacionOportunidadPushAdapter(EvaluarCompatibilidadPrestadorUseCase evaluacion) {
        this.evaluacion = Objects.requireNonNull(evaluacion);
    }

    @Override
    public boolean sigueVigente(UUID solicitudId, UUID prestadorId) {
        return evaluacion.ejecutar(new EvaluarCompatibilidadPrestadorQuery(solicitudId, prestadorId));
    }
}
