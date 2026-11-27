package com.sebastian.spring.mas.aliado.aliado.modules.reportes.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Contratos HTTP de reportes (SPEC-11). */
public final class ReporteDtos {
    private ReporteDtos() {}

    public record CrearRequest(@NotBlank String motivo) {}

    public record ReporteResponse(UUID id, String tipo, UUID objetivoId, String motivo, String estado,
                                  OffsetDateTime fechaCreacion, List<EvidenciaResponse> evidencias) {}

    public record EvidenciaResponse(UUID id, String archivoKey, String tipoContenido) {}

    public record ReportesPropiosPage(List<ReporteResponse> items, int pagina, int tamano,
                                      long totalElementos, int totalPaginas) {}

    public record ReporteIdResponse(UUID reporteId) {}
}
