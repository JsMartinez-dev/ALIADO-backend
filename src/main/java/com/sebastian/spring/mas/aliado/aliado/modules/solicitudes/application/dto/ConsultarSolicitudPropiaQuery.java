package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto;

import java.util.UUID;
public record ConsultarSolicitudPropiaQuery(UUID solicitudId, UUID clienteId) {}
