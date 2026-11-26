package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto;

import java.util.UUID;
import java.time.OffsetDateTime;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.ServicioContratado;

public record ServicioResult(UUID id,UUID solicitudId,UUID propuestaId,UUID clienteId,UUID prestadorId,String estado,boolean comunicacionHabilitada,OffsetDateTime fechaContratacion,SolicitudResumen solicitud,PropuestaResumen propuesta) {
 public record SolicitudResumen(UUID id,String descripcion,UUID categoriaId,UUID zonaId,String urgencia,String estado){}
 public record PropuestaResumen(UUID id,UUID prestadorId,String disponibilidad,String mensaje,Integer tarifaCop,String estado,OffsetDateTime fechaCreacion){}
 public static ServicioResult desde(ServicioContratado s){return new ServicioResult(s.id(),s.solicitudId(),s.propuestaId(),s.clienteId(),s.prestadorId(),s.estado().name(),s.comunicacionHabilitada(),s.fechaContratacion(),null,null);}
}
