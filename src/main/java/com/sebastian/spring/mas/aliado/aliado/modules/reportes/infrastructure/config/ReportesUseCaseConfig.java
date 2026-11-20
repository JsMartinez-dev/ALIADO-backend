package com.sebastian.spring.mas.aliado.aliado.modules.reportes.infrastructure.config;

import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.service.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.usecase.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoStorageService;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.UsuarioConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ResenaConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.repository.EvidenciaReporteRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.repository.ReporteRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.ServicioLecturaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.service.OperacionesReportes;

@Configuration
public class ReportesUseCaseConfig {
    @Bean
    public OperacionesReportes operacionesReportes(ReporteRepository reportes, EvidenciaReporteRepository evidencias,
                          ResenaConsultaPort resenas, UsuarioConsultaPort usuarios,
                          ServicioLecturaPort servicios, ArchivoStorageService storage) {
        return new OperacionesReportes(reportes,evidencias,resenas,usuarios,servicios,new com.sebastian.spring.mas.aliado.aliado.modules.reportes.infrastructure.adapter.ArchivoTransaccionalAdapter(storage));
    }
    @Bean
    public ReportarUsuarioUseCase reportarUsuarioUseCase(@Qualifier("operacionesReportes") OperacionesReportes operaciones, PlatformTransactionManager manager) {
        var service = new ReportarUsuarioService(operaciones);
        var transaction = new TransactionTemplate(manager);
        transaction.setReadOnly(false);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }
    @Bean
    public ReportarServicioUseCase reportarServicioUseCase(@Qualifier("operacionesReportes") OperacionesReportes operaciones, PlatformTransactionManager manager) {
        var service = new ReportarServicioService(operaciones);
        var transaction = new TransactionTemplate(manager);
        transaction.setReadOnly(false);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }
    @Bean
    public AdjuntarEvidenciaReporteUseCase adjuntarEvidenciaReporteUseCase(@Qualifier("operacionesReportes") OperacionesReportes operaciones, PlatformTransactionManager manager) {
        var service = new AdjuntarEvidenciaReporteService(operaciones);
        var transaction = new TransactionTemplate(manager);
        transaction.setReadOnly(false);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }
    @Bean
    public ConsultarReportePropioUseCase consultarReportePropioUseCase(@Qualifier("operacionesReportes") OperacionesReportes operaciones, PlatformTransactionManager manager) {
        var service = new ConsultarReportePropioService(operaciones);
        var transaction = new TransactionTemplate(manager);
        transaction.setReadOnly(true);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }
    @Bean
    public DescargarEvidenciaPropiaUseCase descargarEvidenciaPropiaUseCase(@Qualifier("operacionesReportes") OperacionesReportes operaciones, PlatformTransactionManager manager) {
        var service = new DescargarEvidenciaPropiaService(operaciones);
        var transaction = new TransactionTemplate(manager);
        transaction.setReadOnly(true);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }
}
