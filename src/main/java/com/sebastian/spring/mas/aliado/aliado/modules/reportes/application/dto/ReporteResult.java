package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto;

import java.util.*;
import java.time.OffsetDateTime;

public record ReporteResult(UUID id, String tipo, UUID objetivoId, String motivo, String estado, OffsetDateTime fechaCreacion, List<EvidenciaResult> evidencias) {}
