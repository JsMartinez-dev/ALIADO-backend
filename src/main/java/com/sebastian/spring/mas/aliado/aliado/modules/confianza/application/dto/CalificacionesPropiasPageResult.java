package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CalificacionesPropiasPageResult(List<Item> items, int pagina, int tamano,
                                              long totalElementos, int totalPaginas) {
    public record Item(UUID calificacionId, UUID servicioId, int valor, OffsetDateTime fechaCalificacion,
                       Resena resena) {}
    public record Resena(UUID id, String texto, OffsetDateTime fechaCreacion) {}
}
