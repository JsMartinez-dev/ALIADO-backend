package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto;

import java.util.UUID;
import java.time.OffsetDateTime;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.HistorialEstadoServicio;

public record HistorialServicioResult(String estadoAnterior,String estadoNuevo,UUID actorId,OffsetDateTime fecha) {
 public static HistorialServicioResult desde(HistorialEstadoServicio h){return new HistorialServicioResult(h.estadoAnterior()==null?null:h.estadoAnterior().name(),h.estadoNuevo().name(),h.actorId(),h.fecha());}
}
