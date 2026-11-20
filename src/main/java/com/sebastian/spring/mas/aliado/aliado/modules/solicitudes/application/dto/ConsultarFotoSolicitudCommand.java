package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto;

import java.util.UUID;
public record ConsultarFotoSolicitudCommand(UUID solicitudId, UUID usuarioId, UUID fotoId) {}
