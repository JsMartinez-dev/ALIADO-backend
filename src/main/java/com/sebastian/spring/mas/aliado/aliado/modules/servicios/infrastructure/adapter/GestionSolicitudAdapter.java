package com.sebastian.spring.mas.aliado.aliado.modules.servicios.infrastructure.adapter;

import java.util.*;
import org.springframework.stereotype.Component;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.SolicitudConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.ConsultaGestionSolicitudPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
@Component
public class GestionSolicitudAdapter implements ConsultaGestionSolicitudPort {
    private final SolicitudConsultaPort solicitudConsultaPort;

    public GestionSolicitudAdapter(SolicitudConsultaPort solicitudConsultaPort)
    {this.solicitudConsultaPort = solicitudConsultaPort;
    }
    public Optional<Solicitud> porId(UUID id){
        return solicitudConsultaPort.porId(id);
    }
    public void marcarContratada(UUID id)
    {
        solicitudConsultaPort.marcarContratada(id);
    }
}
