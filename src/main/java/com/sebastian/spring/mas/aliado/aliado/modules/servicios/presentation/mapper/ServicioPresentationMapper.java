package com.sebastian.spring.mas.aliado.aliado.modules.servicios.presentation.mapper;

import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.usecase.*;
import java.util.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.presentation.dto.ServicioDtos;
public final class ServicioPresentationMapper {
 private ServicioPresentationMapper(){}
 public static AccionServicioCommand aCommand(UUID id,UUID actor){return new AccionServicioCommand(id,actor);}
 public static AceptarPropuestaCommand aAceptarCommand(UUID id,UUID actor){return new AceptarPropuestaCommand(id,actor);}
 public static EnviarMensajeServicioCommand aMensajeCommand(UUID id,UUID actor,ServicioDtos.MensajeRequest r){return new EnviarMensajeServicioCommand(id,actor,r.contenido());}
 public static ServicioDtos.ServicioResumenResponse aResumenResponse(ServiciosPropiosResult.Item r){return new ServicioDtos.ServicioResumenResponse(r.id(),r.solicitudId(),r.propuestaId(),r.estado(),r.fechaContratacion());}
 public static ServicioDtos.AceptarResponse aAceptarResponse(ServicioResult r){return new ServicioDtos.AceptarResponse(r.id(),r.estado());}
 public static ServicioDtos.IdEstadoResponse aEstadoResponse(ServicioResult r){return new ServicioDtos.IdEstadoResponse(r.id(),r.estado());}
 public static ServicioDtos.ServicioResponse aResponse(ServicioResult r){
 var s=r.solicitud();var p=r.propuesta();
 return new ServicioDtos.ServicioResponse(r.id(),r.solicitudId(),r.propuestaId(),r.clienteId(),r.prestadorId(),r.estado(),r.comunicacionHabilitada(),r.fechaContratacion(),s==null?null:new ServicioDtos.SolicitudResumen(s.id(),s.descripcion(),s.categoriaId(),s.zonaId(),s.urgencia(),s.estado()),p==null?null:new ServicioDtos.PropuestaResumen(p.id(),p.prestadorId(),p.disponibilidad(),p.mensaje(),p.tarifaCop(),p.estado(),p.fechaCreacion()));}
 public static ServicioDtos.HistorialResponse aHistorialResponse(HistorialServicioResult r){return new ServicioDtos.HistorialResponse(r.estadoAnterior(),r.estadoNuevo(),r.actorId(),r.fecha());}
 public static ServicioDtos.MensajeResponse aMensajeResponse(MensajeServicioResult r){return new ServicioDtos.MensajeResponse(r.id(),r.remitenteId(),r.contenido(),r.fechaEnvio());}
}
