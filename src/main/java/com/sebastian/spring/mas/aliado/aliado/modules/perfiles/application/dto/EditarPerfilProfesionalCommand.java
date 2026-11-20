package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto;
import java.util.Map;
import java.util.UUID;
public record EditarPerfilProfesionalCommand(UUID prestadorId, Map<String, Object> datosGenerales) {}
