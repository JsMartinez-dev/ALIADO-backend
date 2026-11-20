package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto;

import java.util.UUID;

/** Datos de UC017; publicar exige ambos campos y un borrador permite omitirlos. */
public record DefinirZonaUrgenciaCommand(UUID zonaId, String urgencia, boolean publicar) {}
