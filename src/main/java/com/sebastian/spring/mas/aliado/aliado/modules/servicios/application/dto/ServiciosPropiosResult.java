package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto;

import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.ServicioContratado;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ServiciosPropiosResult(List<Item> items, int pagina, int tamano,
                                     long totalElementos, int totalPaginas) {
    public record Item(UUID id, UUID solicitudId, UUID propuestaId, String estado,
                       OffsetDateTime fechaContratacion) {}

    public static ServiciosPropiosResult desde(List<ServicioContratado> servicios, int pagina, int tamano,
                                                long total) {
        int totalPaginas = total == 0 ? 0 : (int) ((total + tamano - 1) / tamano);
        return new ServiciosPropiosResult(servicios.stream()
                .map(s -> new Item(s.id(), s.solicitudId(), s.propuestaId(), s.estado().name(),
                        s.fechaContratacion()))
                .toList(), pagina, tamano, total, totalPaginas);
    }
}
