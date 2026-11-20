package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Urgencia;
import java.util.UUID;

public record ZonaUrgenciaResult(UUID zonaId, Urgencia urgencia) {}
