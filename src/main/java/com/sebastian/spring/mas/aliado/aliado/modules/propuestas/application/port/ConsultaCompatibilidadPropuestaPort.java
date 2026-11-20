package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.port;
import java.util.UUID;
public interface ConsultaCompatibilidadPropuestaPort { boolean esCompatible(UUID solicitudId, UUID prestadorId); }
