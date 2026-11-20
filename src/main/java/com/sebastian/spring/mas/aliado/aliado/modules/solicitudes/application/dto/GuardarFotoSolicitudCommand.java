package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto;

import java.util.UUID;
public record GuardarFotoSolicitudCommand(UUID solicitudId, UUID usuarioId, UUID fotoId,
        String nombreOriginal, String tipoContenido, byte[] contenido) {}
