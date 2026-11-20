package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.port;
import java.util.UUID;
public interface RegistroNotificacionPropuestaPort { void registrar(UUID propuestaId, UUID clienteId); }
