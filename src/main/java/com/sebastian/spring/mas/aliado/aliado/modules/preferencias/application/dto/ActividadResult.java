package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ActividadResult(String tipo, String resumen, OffsetDateTime fecha,
                              String referenciaTipo, UUID referenciaId) {}
