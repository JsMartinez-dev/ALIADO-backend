package com.sebastian.spring.mas.aliado.aliado.modules.servicios.infrastructure.config;

import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.usecase.*;
import java.util.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.service.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.repository.ServicioRepository;
import com.sebastian.spring.mas.aliado.aliado.common.events.PublicadorEventos;
import org.springframework.context.annotation.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
@Configuration
public class ServicioUseCaseConfig {
 private TransactionTemplate tx(PlatformTransactionManager m,boolean lectura){var t=new TransactionTemplate(m);t.setReadOnly(lectura);return t;}
 @Bean public ListarPropuestasRecibidasUseCase listarPropuestasRecibidasUseCase(ConsultaGestionSolicitudPort solicitudes,ConsultaGestionPropuestasPort propuestas,PlatformTransactionManager manager){var service=new ListarPropuestasRecibidasService(solicitudes,propuestas);var transaction=tx(manager,true);return command->transaction.execute(status->service.ejecutar(command));}
 @Bean public AceptarPropuestaUseCase aceptarPropuestaUseCase(ServicioRepository repo,ConsultaGestionPropuestasPort propuestas,ConsultaGestionSolicitudPort solicitudes,PublicadorEventos eventos,RegistrarServicioContratadoUseCase registrar,CerrarPropuestasNoSeleccionadasUseCase cerrar,PlatformTransactionManager manager){var service=new AceptarPropuestaService(repo,propuestas,solicitudes,eventos,registrar,cerrar);var transaction=tx(manager,false);return command->transaction.execute(status->service.ejecutar(command));}
 @Bean public ConsultarServiciosPropiosUseCase consultarServiciosPropiosUseCase(ServicioRepository repo,PlatformTransactionManager manager){var service=new ConsultarServiciosPropiosService(repo);var transaction=tx(manager,true);return (usuarioId,pagina,tamano)->transaction.execute(status->service.ejecutar(usuarioId,pagina,tamano));}
 @Bean public ConsultarServicioUseCase consultarServicioUseCase(ServicioRepository repo,ConsultaGestionSolicitudPort solicitudes,ConsultaGestionPropuestasPort propuestas,PlatformTransactionManager manager){var service=new ConsultarServicioService(repo,solicitudes,propuestas);var transaction=tx(manager,true);return command->transaction.execute(status->service.ejecutar(command));}
 @Bean public MarcarServicioEnEjecucionUseCase marcarServicioEnEjecucionUseCase(ServicioRepository repo,PublicadorEventos eventos,PlatformTransactionManager manager){var service=new MarcarServicioEnEjecucionService(repo,eventos);var transaction=tx(manager,false);return command->transaction.execute(status->service.ejecutar(command));}
 @Bean public MarcarServicioTerminadoUseCase marcarServicioTerminadoUseCase(ServicioRepository repo,PublicadorEventos eventos,PlatformTransactionManager manager){var service=new MarcarServicioTerminadoService(repo,eventos);var transaction=tx(manager,false);return command->transaction.execute(status->service.ejecutar(command));}
 @Bean public ConfirmarFinalizacionServicioUseCase confirmarFinalizacionServicioUseCase(ServicioRepository repo,PublicadorEventos eventos,PlatformTransactionManager manager){var service=new ConfirmarFinalizacionServicioService(repo,eventos);var transaction=tx(manager,false);return command->transaction.execute(status->service.ejecutar(command));}
 @Bean public CancelarServicioUseCase cancelarServicioUseCase(ServicioRepository repo,PublicadorEventos eventos,PlatformTransactionManager manager){var service=new CancelarServicioService(repo,eventos);var transaction=tx(manager,false);return command->transaction.execute(status->service.ejecutar(command));}
 @Bean public ConsultarHistorialServicioUseCase consultarHistorialServicioUseCase(ServicioRepository repo,PlatformTransactionManager manager){var service=new ConsultarHistorialServicioService(repo);var transaction=tx(manager,true);return command->transaction.execute(status->service.ejecutar(command));}
 @Bean public EnviarMensajeServicioUseCase enviarMensajeServicioUseCase(ServicioRepository repo,PlatformTransactionManager manager){var service=new EnviarMensajeServicioService(repo);var transaction=tx(manager,false);return command->transaction.execute(status->service.ejecutar(command));}
 @Bean public ConsultarMensajesServicioUseCase consultarMensajesServicioUseCase(ServicioRepository repo,PlatformTransactionManager manager){var service=new ConsultarMensajesServicioService(repo);var transaction=tx(manager,true);return command->transaction.execute(status->service.ejecutar(command));}
 @Bean public HabilitarComunicacionServicioUseCase habilitarComunicacionServicioUseCase(){return new HabilitarComunicacionServicioService();}
 @Bean public RegistrarServicioContratadoUseCase registrarServicioContratadoUseCase(ServicioRepository r,HabilitarComunicacionServicioUseCase h){return new RegistrarServicioContratadoService(r,h);}
 @Bean public CerrarPropuestasNoSeleccionadasUseCase cerrarPropuestasNoSeleccionadasUseCase(ConsultaGestionPropuestasPort p){return new CerrarPropuestasNoSeleccionadasService(p);}
}
