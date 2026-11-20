package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto;

import java.util.UUID;
import java.time.OffsetDateTime;

public record ServicioResult(UUID id, String estado, OffsetDateTime fechaContratacion) {}
