package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.EstadoOportunidadAvisoPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoOportunidadAviso;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.ResolverEstadoOportunidadQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.ResolverEstadoOportunidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.domain.model.EstadoOportunidad;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Adaptador del estado de la oportunidad para la consulta desde un aviso (F09-PUSH-10).
 *
 * <p>El puerto lo define notificaciones. Este adaptador consume el caso de uso público de
 * oportunidades, que reutiliza la evaluación de compatibilidad del detalle; así el módulo
 * de notificaciones no accede a repositorios ni adaptadores de persistencia ajenos.</p>
 */
@Component
public class EstadoOportunidadAvisoAdapter implements EstadoOportunidadAvisoPort {

    private final ResolverEstadoOportunidadUseCase resolverEstado;

    public EstadoOportunidadAvisoAdapter(ResolverEstadoOportunidadUseCase resolverEstado) {
        this.resolverEstado = Objects.requireNonNull(resolverEstado);
    }

    @Override
    public Optional<EstadoOportunidadAviso> estado(UUID solicitudId, UUID prestadorId) {
        return resolverEstado.ejecutar(new ResolverEstadoOportunidadQuery(solicitudId, prestadorId))
                .map(this::aEstadoAviso);
    }

    private EstadoOportunidadAviso aEstadoAviso(EstadoOportunidad estado) {
        return switch (estado) {
            case ABIERTA -> EstadoOportunidadAviso.ABIERTA;
            case CERRADA -> EstadoOportunidadAviso.CERRADA;
        };
    }
}
