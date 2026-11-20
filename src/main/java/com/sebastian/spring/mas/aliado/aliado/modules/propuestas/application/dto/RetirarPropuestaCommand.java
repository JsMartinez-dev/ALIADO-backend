package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.dto;
import java.util.UUID;
public record RetirarPropuestaCommand(UUID propuestaId, UUID prestadorId) {}
