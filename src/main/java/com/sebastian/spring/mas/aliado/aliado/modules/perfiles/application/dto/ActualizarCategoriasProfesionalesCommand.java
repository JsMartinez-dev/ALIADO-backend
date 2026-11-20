package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto;
import java.util.List;
import java.util.UUID;
public record ActualizarCategoriasProfesionalesCommand(UUID prestadorId, List<UUID> categoriaIds) {}
