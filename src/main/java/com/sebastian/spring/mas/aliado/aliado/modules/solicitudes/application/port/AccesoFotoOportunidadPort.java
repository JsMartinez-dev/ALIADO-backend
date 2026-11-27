package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port;

import java.util.UUID;

/** Consulta si un Prestador puede ver la oportunidad asociada a una foto. */
public interface AccesoFotoOportunidadPort {
    boolean prestadorPuedeVer(UUID solicitudId, UUID prestadorId);
}
