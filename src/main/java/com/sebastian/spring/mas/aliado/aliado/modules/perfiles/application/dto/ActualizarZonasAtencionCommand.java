package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto;
import java.util.List;
import java.util.UUID;
public record ActualizarZonasAtencionCommand(UUID prestadorId, List<UUID> zonaIds) {}
