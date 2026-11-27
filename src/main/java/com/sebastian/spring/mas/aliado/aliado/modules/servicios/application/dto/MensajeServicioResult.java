package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto;

import java.util.UUID;
import java.time.OffsetDateTime;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.MensajeServicio;

public record MensajeServicioResult(UUID id,UUID remitenteId,String contenido,boolean remitenteEliminado,OffsetDateTime fechaEnvio) {
 public static MensajeServicioResult desde(MensajeServicio m){return desde(m,false);}
 public static MensajeServicioResult desde(MensajeServicio m, boolean remitenteEliminado){return new MensajeServicioResult(m.id(),m.remitenteId(),m.contenido(),remitenteEliminado,m.fechaEnvio());}
}
