package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.CompatibilidadPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.AccesoFotoOportunidadPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Reutiliza la misma regla que habilita el detalle de una oportunidad. */
@Component
public class AccesoFotoOportunidadAdapter implements AccesoFotoOportunidadPort {
    private final CompatibilidadPort compatibilidad;

    public AccesoFotoOportunidadAdapter(CompatibilidadPort compatibilidad) {
        this.compatibilidad = compatibilidad;
    }

    @Override
    public boolean prestadorPuedeVer(UUID solicitudId, UUID prestadorId) {
        return compatibilidad.esCompatible(solicitudId, prestadorId);
    }
}
