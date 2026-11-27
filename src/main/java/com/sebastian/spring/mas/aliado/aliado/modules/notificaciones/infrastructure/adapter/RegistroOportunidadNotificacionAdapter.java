package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RegistrarAvisoOportunidadCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RegistrarAvisoOportunidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.RegistroNotificacionPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Conecta el matching de oportunidades con el registro del aviso y el encolado push
 * del piloto (F09-PUSH-05). Toda la operación ocurre en la transacción de publicación.
 */
@Component
public class RegistroOportunidadNotificacionAdapter implements RegistroNotificacionPort {
    private final RegistrarAvisoOportunidadUseCase registrarAviso;

    public RegistroOportunidadNotificacionAdapter(RegistrarAvisoOportunidadUseCase registrarAviso) {
        this.registrarAviso = registrarAviso;
    }

    @Override
    public void registrar(UUID solicitudId, UUID prestadorId, int version, String detalle) {
        registrarAviso.ejecutar(new RegistrarAvisoOportunidadCommand(solicitudId, prestadorId, version, detalle));
    }
}
