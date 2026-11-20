package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto;

import java.util.List;

public record ReportesPropiosPageResult(List<ReporteResult> items, int pagina, int tamano,
                                        long totalElementos, int totalPaginas) {}
