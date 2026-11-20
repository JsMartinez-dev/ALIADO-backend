package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto;

import java.util.UUID;
import java.time.OffsetDateTime;

public record AccionServicioCommand(UUID servicioId, UUID actorId) {}
