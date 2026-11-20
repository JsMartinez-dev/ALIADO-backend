package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.infrastructure.config;
import com.sebastian.spring.mas.aliado.aliado.common.events.PublicadorEventos;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.port.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.service.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.repository.PropuestaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
@Configuration
public class PropuestaUseCaseConfig {
    @Bean public ValidarDisponibilidadPropuestaUseCase validarDisponibilidadPropuestaUseCase() {
        return new ValidarDisponibilidadPropuestaService();
    }
    @Bean public ValidarMensajePropuestaUseCase validarMensajePropuestaUseCase() {
        return new ValidarMensajePropuestaService();
    }
    @Bean public CrearPropuestaUseCase crearPropuestaUseCase(PropuestaRepository propuestas,
            ConsultaSolicitudPropuestaPort solicitudes, ConsultaCompatibilidadPropuestaPort compatibilidad,
            PublicadorEventos eventos, ValidarDisponibilidadPropuestaUseCase disponibilidad,
            ValidarMensajePropuestaUseCase mensaje, PlatformTransactionManager manager) {
        var service = new CrearPropuestaService(propuestas, solicitudes, compatibilidad, eventos, disponibilidad, mensaje);
        var transaction = new TransactionTemplate(manager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }
    @Bean public EditarPropuestaUseCase editarPropuestaUseCase(PropuestaRepository propuestas,
            ValidarDisponibilidadPropuestaUseCase disponibilidad, ValidarMensajePropuestaUseCase mensaje,
            PlatformTransactionManager manager) {
        var service = new EditarPropuestaService(propuestas, disponibilidad, mensaje);
        var transaction = new TransactionTemplate(manager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }
    @Bean public RetirarPropuestaUseCase retirarPropuestaUseCase(PropuestaRepository propuestas, PlatformTransactionManager manager) {
        var service = new RetirarPropuestaService(propuestas);
        var transaction = new TransactionTemplate(manager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }
    @Bean public NotificarPropuestaRecibidaUseCase notificarPropuestaRecibidaUseCase(PropuestaRepository propuestas,
            ConsultaSolicitudPropuestaPort solicitudes, RegistroNotificacionPropuestaPort registro, PlatformTransactionManager manager) {
        var service = new NotificarPropuestaRecibidaService(propuestas, solicitudes, registro);
        var transaction = new TransactionTemplate(manager);
        return propuestaId -> transaction.executeWithoutResult(status -> service.ejecutar(propuestaId));
    }
}
