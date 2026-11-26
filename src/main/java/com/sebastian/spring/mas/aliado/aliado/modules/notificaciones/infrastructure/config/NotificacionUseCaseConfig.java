package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.config;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.ConsultarNotificacionesOportunidadesService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.RegistrarAvisoOportunidadService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.ResolverOportunidadAvisoService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.EstadoOportunidadAvisoPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ResolverOportunidadAvisoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.RegistrarDispositivoPushService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.RetirarDispositivoPushService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.RevocarVinculoPushDiferidoService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarNotificacionesOportunidadesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RegistrarAvisoOportunidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RegistrarDispositivoPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RetirarDispositivoPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RevocarVinculoPushDiferidoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.DispositivoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.TrabajoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ConsultaPreferenciasPort;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
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

 /** Aviso de oportunidad y trabajo push dentro de la transacción de publicación (F09-PUSH-05). */
 @Bean public RegistrarAvisoOportunidadUseCase registrarAvisoOportunidadUseCase(
   NotificacionRepository notificaciones, TrabajoPushRepository trabajos,
   ConsultaPreferenciasPort preferencias,
   @Value("${aliado.push.vigencia-minutos:60}") long vigenciaMinutos) {
  return new RegistrarAvisoOportunidadService(notificaciones, trabajos, preferencias,
    Duration.ofMinutes(vigenciaMinutos));
 }

 /** Estado permitido al destinatario del aviso, dentro de una lectura consistente (F09-PUSH-10). */
 @Bean public ResolverOportunidadAvisoUseCase resolverOportunidadAvisoUseCase(
   NotificacionRepository notificaciones, EstadoOportunidadAvisoPort estadoOportunidad,
   PlatformTransactionManager manager) {
  var service = new ResolverOportunidadAvisoService(notificaciones, estadoOportunidad);
  var transaction = new TransactionTemplate(manager);
  transaction.setReadOnly(true);
  return command -> transaction.execute(status -> service.ejecutar(command));
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

 /** Alta y sincronización de la instalación: una sola transacción para generación y vínculo. */
 @Bean public RegistrarDispositivoPushUseCase registrarDispositivoPushUseCase(
   DispositivoPushRepository dispositivos, PlatformTransactionManager manager) {
  var service = new RegistrarDispositivoPushService(dispositivos);
  var transaction = new TransactionTemplate(manager);
  return command -> transaction.executeWithoutResult(status -> service.ejecutar(command));
 }

 /** Baja en línea al cerrar sesión. */
 @Bean public RetirarDispositivoPushUseCase retirarDispositivoPushUseCase(
   DispositivoPushRepository dispositivos, PlatformTransactionManager manager) {
  var service = new RetirarDispositivoPushService(dispositivos);
  var transaction = new TransactionTemplate(manager);
  return command -> transaction.executeWithoutResult(status -> service.ejecutar(command));
 }

 /** Baja diferida sin sesión, limitada a la generación de la credencial. */
 @Bean public RevocarVinculoPushDiferidoUseCase revocarVinculoPushDiferidoUseCase(
   DispositivoPushRepository dispositivos, PlatformTransactionManager manager) {
  var service = new RevocarVinculoPushDiferidoService(dispositivos);
  var transaction = new TransactionTemplate(manager);
  return command -> transaction.executeWithoutResult(status -> service.ejecutar(command));
 }
}
