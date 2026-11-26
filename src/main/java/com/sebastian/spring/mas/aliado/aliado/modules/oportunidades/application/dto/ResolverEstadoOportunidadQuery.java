package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto;

import java.util.UUID;

/** Consulta del estado mínimo de una oportunidad para un Prestador (F09-PUSH-10). */
public record ResolverEstadoOportunidadQuery(UUID solicitudId, UUID prestadorId) {}
