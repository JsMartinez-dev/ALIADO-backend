package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto;

import java.util.UUID;

public record ActualizarPerfilCommand(UUID usuarioId, String nombre, String telefono, Integer version) {}
