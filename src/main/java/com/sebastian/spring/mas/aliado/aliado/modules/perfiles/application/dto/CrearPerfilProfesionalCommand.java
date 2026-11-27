package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto;

import java.util.List;
import java.util.UUID;

public record CrearPerfilProfesionalCommand(UUID prestadorId, String descripcion,
        List<UUID> categoriaIds, List<UUID> zonaIds, List<ExperienciaDatos> experiencias) {}
