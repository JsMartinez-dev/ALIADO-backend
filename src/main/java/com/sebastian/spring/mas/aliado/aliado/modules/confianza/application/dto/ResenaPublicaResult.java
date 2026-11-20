package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto;

import java.util.UUID;
import java.time.OffsetDateTime;

public record ResenaPublicaResult(UUID id, int valor, String texto, OffsetDateTime fechaCreacion) {}
