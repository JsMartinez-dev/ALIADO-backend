package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto;

import java.util.UUID;

public record ActualizarPreferenciaCommand(UUID usuarioId, String tipoAviso, String canal, boolean habilitada) {}
