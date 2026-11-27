package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto;

import java.util.UUID;

public record AdjuntarEvidenciaIdempotenteCommand(UUID reporteId, UUID reportanteId, String nombreOriginal,
                                                 String tipoContenido, byte[] contenido,
                                                 UUID claveIdempotencia) {}
