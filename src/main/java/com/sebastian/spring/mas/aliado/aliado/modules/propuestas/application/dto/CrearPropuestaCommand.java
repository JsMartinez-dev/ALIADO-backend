package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.dto;
import java.util.UUID;
public record CrearPropuestaCommand(UUID prestadorId, UUID solicitudId, String disponibilidad, String mensaje) {}
