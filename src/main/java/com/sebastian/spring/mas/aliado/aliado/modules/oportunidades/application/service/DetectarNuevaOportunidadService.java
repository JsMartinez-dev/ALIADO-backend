package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.CompatibilidadPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.RegistroNotificacionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.DetectarNuevaOportunidadUseCase;
import java.util.UUID;
public class DetectarNuevaOportunidadService implements DetectarNuevaOportunidadUseCase {
 private final CompatibilidadPort compatibilidad;
 private final RegistroNotificacionPort registro;
 public DetectarNuevaOportunidadService(CompatibilidadPort compatibilidad, RegistroNotificacionPort registro) {
  this.compatibilidad=compatibilidad; this.registro=registro;
 }
 @Override public void ejecutar(UUID solicitudId) {
  for(UUID prestadorId:compatibilidad.prestadoresCompatibles(solicitudId)) {
   registro.registrar(solicitudId,prestadorId,1,"Nueva oportunidad compatible con tu perfil.");
  }
 }
}
