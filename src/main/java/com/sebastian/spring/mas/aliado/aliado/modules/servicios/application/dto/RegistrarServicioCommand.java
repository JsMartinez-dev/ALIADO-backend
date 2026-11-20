package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto;

import java.util.UUID;
import java.time.OffsetDateTime;

public record RegistrarServicioCommand(UUID solicitudId, UUID propuestaId, UUID clienteId, UUID prestadorId, OffsetDateTime fecha) {}
