package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto;

import java.util.UUID;

public record ActualizarEnlaceInstitucionalCommand(
    UUID enlaceId, String nombre, String url, String estado) {}
