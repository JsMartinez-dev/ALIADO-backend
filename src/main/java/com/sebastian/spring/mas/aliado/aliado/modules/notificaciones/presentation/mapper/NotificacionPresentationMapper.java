package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.mapper;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.NotificacionOportunidadResult;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.dto.NotificacionDtos;
public final class NotificacionPresentationMapper {
 private NotificacionPresentationMapper() {}
 public static NotificacionDtos.NotificacionResponse aResponse(NotificacionOportunidadResult result) {
  return new NotificacionDtos.NotificacionResponse(result.id(),"NUEVA_OPORTUNIDAD",null,result.solicitudId(),
    result.detalle(),result.estadoEntrega(),result.fechaGeneracion());
 }
 public static NotificacionDtos.NotificacionResponse aResponse(com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.NotificacionResult result) {
  return new NotificacionDtos.NotificacionResponse(result.id(), result.tipo(), result.servicioId(), result.solicitudId(),
    result.detalle(), result.estadoEntrega(), result.fechaGeneracion());
 }
}
