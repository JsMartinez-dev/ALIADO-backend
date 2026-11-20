package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.config;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.ConsultarNotificacionesOportunidadesService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarNotificacionesOportunidadesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
@Configuration
public class NotificacionUseCaseConfig {
 @Bean public ConsultarNotificacionesOportunidadesUseCase consultarNotificacionesOportunidadesUseCase(
   NotificacionRepository notificaciones,PlatformTransactionManager transactionManager) {
  var service=new ConsultarNotificacionesOportunidadesService(notificaciones);
  var transaction=new TransactionTemplate(transactionManager);
  transaction.setReadOnly(true);
  return prestadorId->transaction.execute(status->service.ejecutar(prestadorId));
 }

 @Bean public com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.NotificarAceptacionPropuestaUseCase notificarAceptacionPropuesta(com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.NotificacionPort port) { return new com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.NotificarAceptacionPropuestaService(port); }

 @Bean public com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.NotificarCambioEstadoServicioUseCase notificarCambioEstadoServicio(com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.NotificacionPort port) { return new com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.NotificarCambioEstadoServicioService(port); }

 @Bean public com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarNotificacionesUseCase consultarNotificacionesUseCase(
   com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.NotificacionConsultaPort consultas, PlatformTransactionManager manager) {
  var service = new com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.ConsultarNotificacionesService(consultas);
  var transaction = new TransactionTemplate(manager);
  transaction.setReadOnly(true);
  return command -> transaction.execute(status -> service.ejecutar(command));
 }
}
