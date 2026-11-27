package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto;

import java.util.UUID;

public record ConsultarCalificacionPropiaCommand(UUID servicioId, UUID clienteId) {}
