package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto;

import java.util.UUID;

public record PublicarSolicitudCommand(UUID solicitudId, UUID clienteId) {}
