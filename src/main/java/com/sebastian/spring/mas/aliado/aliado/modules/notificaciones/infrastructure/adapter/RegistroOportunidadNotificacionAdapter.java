package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.RegistroNotificacionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.NotificacionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Canal;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import java.util.UUID;
import org.springframework.stereotype.Component;
@Component
public class RegistroOportunidadNotificacionAdapter implements RegistroNotificacionPort {
 private final NotificacionPort notificaciones;
 public RegistroOportunidadNotificacionAdapter(NotificacionPort notificaciones) {this.notificaciones=notificaciones;}
 @Override public void registrar(UUID solicitudId, UUID prestadorId, int version, String detalle) {
  notificaciones.registrar(TipoNotificacion.NUEVA_OPORTUNIDAD,solicitudId,prestadorId,version,detalle,Canal.PUSH);
 }
}
