package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CrearPerfilProfesionalCommand(UUID prestadorId, Map<String, Object> datosGenerales,
        List<UUID> categoriaIds, List<UUID> zonaIds, List<ExperienciaDatos> experiencias) {}
