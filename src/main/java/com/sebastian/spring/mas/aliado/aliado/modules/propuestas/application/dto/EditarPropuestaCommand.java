package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.dto;
import java.util.UUID;
public record EditarPropuestaCommand(UUID propuestaId, UUID prestadorId, String disponibilidad, String mensaje) {}
