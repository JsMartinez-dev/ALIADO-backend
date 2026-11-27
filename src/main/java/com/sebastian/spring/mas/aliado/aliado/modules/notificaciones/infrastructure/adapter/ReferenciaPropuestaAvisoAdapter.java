package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.ReferenciaPropuestaAvisoPort;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.port.PropuestaConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.SolicitudConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ReferenciaPropuestaAvisoAdapter implements ReferenciaPropuestaAvisoPort {
    private final PropuestaConsultaPort propuestas;
    private final SolicitudConsultaPort solicitudes;

    public ReferenciaPropuestaAvisoAdapter(PropuestaConsultaPort propuestas, SolicitudConsultaPort solicitudes) {
        this.propuestas = propuestas;
        this.solicitudes = solicitudes;
    }

    @Override
    public Optional<UUID> solicitudPropia(UUID propuestaId, UUID destinatarioId) {
        return propuestas.porId(propuestaId)
                .flatMap(propuesta -> solicitudes.porId(propuesta.solicitudId()))
                .filter(solicitud -> solicitud.clienteId().equals(destinatarioId))
                .map(Solicitud::id);
    }
}
