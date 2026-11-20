package com.sebastian.spring.mas.aliado.aliado.modules.confianza.infrastructure.config;

import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.RolConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.CalificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.ReputacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.ResenaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.SolicitudVerificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.port.ReportePort;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.ServicioLecturaPort;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service.OperacionesConfianza;
import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoStorageService;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.DocumentoVerificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service.OperacionesVerificacion;

@Configuration
public class ConfianzaUseCaseConfig {
    @Bean
    public OperacionesConfianza operacionesConfianza(CalificacionRepository calificaciones, ResenaRepository resenas,
                            ReputacionRepository reputaciones,
                            SolicitudVerificacionRepository verificaciones,
                            ServicioLecturaPort servicios, ReportePort reportes,
                            RolConsultaPort roles) {
        return new OperacionesConfianza(calificaciones,resenas,reputaciones,verificaciones,servicios,reportes,roles);
    }
    @Bean
    public OperacionesVerificacion operacionesVerificacion(SolicitudVerificacionRepository verificaciones,
                               DocumentoVerificacionRepository documentos, ArchivoStorageService storage) {
        return new OperacionesVerificacion(verificaciones,documentos,new com.sebastian.spring.mas.aliado.aliado.modules.confianza.infrastructure.adapter.ArchivoTransaccionalAdapter(storage));
    }
    @Bean
    public CalificarServicioUseCase calificarServicioUseCase(@Qualifier("operacionesConfianza") OperacionesConfianza operaciones, RecalcularReputacionUseCase recalcular, PlatformTransactionManager manager) {
        var service = new CalificarServicioService(operaciones, recalcular);
        var transaction = new TransactionTemplate(manager);
        transaction.setReadOnly(false);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }
    @Bean
    public ConsultarCalificacionPropiaUseCase consultarCalificacionPropiaUseCase(
            CalificacionRepository calificaciones, ResenaRepository resenas, ServicioLecturaPort servicios,
            PlatformTransactionManager manager) {
        var service = new ConsultarCalificacionPropiaService(calificaciones, resenas, servicios);
        var transaction = new TransactionTemplate(manager);
        transaction.setReadOnly(true);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public ConsultarCalificacionesPropiasUseCase consultarCalificacionesPropiasUseCase(
            CalificacionRepository calificaciones, ResenaRepository resenas, PlatformTransactionManager manager) {
        var service = new ConsultarCalificacionesPropiasService(calificaciones, resenas);
        var transaction = new TransactionTemplate(manager);
        transaction.setReadOnly(true);
        return (clienteId, pagina, tamano) -> transaction.execute(status -> service.ejecutar(clienteId, pagina, tamano));
    }

    @Bean
    public AgregarResenaUseCase agregarResenaUseCase(@Qualifier("operacionesConfianza") OperacionesConfianza operaciones, PlatformTransactionManager manager) {
        var service = new AgregarResenaService(operaciones);
        var transaction = new TransactionTemplate(manager);
        transaction.setReadOnly(false);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }
    @Bean
    public RecalcularReputacionUseCase recalcularReputacionUseCase(@Qualifier("operacionesConfianza") OperacionesConfianza operaciones, PlatformTransactionManager manager) {
        var service = new RecalcularReputacionService(operaciones);
        var transaction = new TransactionTemplate(manager);
        transaction.setReadOnly(false);
        return command -> transaction.executeWithoutResult(status -> service.ejecutar(command));
    }
    @Bean
    public ConsultarConfianzaUseCase consultarConfianzaUseCase(@Qualifier("operacionesConfianza") OperacionesConfianza operaciones, PlatformTransactionManager manager) {
        var service = new ConsultarConfianzaService(operaciones);
        var transaction = new TransactionTemplate(manager);
        transaction.setReadOnly(true);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }
    @Bean
    public ConsultarResenasPublicasUseCase consultarResenasPublicasUseCase(@Qualifier("operacionesConfianza") OperacionesConfianza operaciones, PlatformTransactionManager manager) {
        var service = new ConsultarResenasPublicasService(operaciones);
        var transaction = new TransactionTemplate(manager);
        transaction.setReadOnly(true);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }
    @Bean
    public ReportarResenaUseCase reportarResenaUseCase(@Qualifier("operacionesConfianza") OperacionesConfianza operaciones, PlatformTransactionManager manager) {
        var service = new ReportarResenaService(operaciones);
        var transaction = new TransactionTemplate(manager);
        transaction.setReadOnly(false);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }
    @Bean
    public SolicitarVerificacionUseCase solicitarVerificacionUseCase(@Qualifier("operacionesVerificacion") OperacionesVerificacion operaciones, PlatformTransactionManager manager) {
        var service = new SolicitarVerificacionService(operaciones);
        var transaction = new TransactionTemplate(manager);
        transaction.setReadOnly(false);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }
    @Bean
    public ConsultarVerificacionPropiaUseCase consultarVerificacionPropiaUseCase(@Qualifier("operacionesVerificacion") OperacionesVerificacion operaciones, PlatformTransactionManager manager) {
        var service = new ConsultarVerificacionPropiaService(operaciones);
        var transaction = new TransactionTemplate(manager);
        transaction.setReadOnly(true);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }
}
