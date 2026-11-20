package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.infrastructure.adapter;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.port.ConsultaSolicitudPropuestaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.port.ConsultaCompatibilidadPropuestaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.SolicitudConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.CompatibilidadPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
@Component
public class ConsultaPropuestaAdapter implements ConsultaSolicitudPropuestaPort, ConsultaCompatibilidadPropuestaPort {
    private final SolicitudConsultaPort solicitudes;
    private final CompatibilidadPort compatibilidad;
    public ConsultaPropuestaAdapter(SolicitudConsultaPort solicitudes, CompatibilidadPort compatibilidad) {
        this.solicitudes = solicitudes; this.compatibilidad = compatibilidad;
    }
    @Override public Optional<Solicitud> porId(UUID id) { return solicitudes.porId(id); }
    @Override public boolean esCompatible(UUID solicitudId, UUID prestadorId) { return compatibilidad.esCompatible(solicitudId, prestadorId); }
}
