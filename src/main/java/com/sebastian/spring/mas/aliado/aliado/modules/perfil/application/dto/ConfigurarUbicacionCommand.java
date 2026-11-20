package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto;

import java.util.UUID;

public record ConfigurarUbicacionCommand(UUID usuarioId, String zonaAproximada, String direccionExacta) {}
