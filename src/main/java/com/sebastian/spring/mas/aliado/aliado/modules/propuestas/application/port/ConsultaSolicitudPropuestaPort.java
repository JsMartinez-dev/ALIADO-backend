package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.port;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import java.util.Optional;
import java.util.UUID;
public interface ConsultaSolicitudPropuestaPort { Optional<Solicitud> porId(UUID solicitudId); }
