package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.NotificacionOportunidadResult;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarNotificacionesOportunidadesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import java.util.List;
import java.util.UUID;
public class ConsultarNotificacionesOportunidadesService implements ConsultarNotificacionesOportunidadesUseCase {
 private final NotificacionRepository notificaciones;
 public ConsultarNotificacionesOportunidadesService(NotificacionRepository notificaciones) {this.notificaciones=notificaciones;}
 @Override public List<NotificacionOportunidadResult> ejecutar(UUID prestadorId) {
  return notificaciones.porDestinatarioYTipo(prestadorId,TipoNotificacion.NUEVA_OPORTUNIDAD.name()).stream()
   .map(n->new NotificacionOportunidadResult(n.id(),n.aggregateId(),n.detalle(),n.estadoEntrega().name(),n.fechaGeneracion())).toList();
 }
}
