package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto;

import java.util.UUID;

public record ConfigurarPrivacidadCommand(UUID usuarioId, String preferenciaVisibilidad) {}
