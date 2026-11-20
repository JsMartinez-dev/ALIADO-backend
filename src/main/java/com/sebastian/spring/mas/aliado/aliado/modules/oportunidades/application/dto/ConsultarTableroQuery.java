package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto;
import java.util.UUID;
public record ConsultarTableroQuery(UUID prestadorId, String categoria, String urgencia) {}
