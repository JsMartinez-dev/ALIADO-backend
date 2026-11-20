package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.infrastructure.config;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ListarPreferenciasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service.ListarPreferenciasService;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ActualizarPreferenciaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service.ActualizarPreferenciaService;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ConsultarHistorialUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service.ConsultarHistorialService;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ExportarHistorialUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service.ExportarHistorialService;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.DescargarHistorialUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service.DescargarHistorialService;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ConsultarIndicadoresPrestadorUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service.ConsultarIndicadoresPrestadorService;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ConsultarActividadRecienteUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service.ConsultarActividadRecienteService;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.HistorialServiciosPort;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.IndicadoresPrestadorPort;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ArchivoHistorialPort;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ActividadNotificacionesPort;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.repository.PreferenciaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.repository.DescargaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class PreferenciasUseCaseConfig {
    @Bean
    public ListarPreferenciasUseCase listarPreferencias(
            PlatformTransactionManager manager, PreferenciaRepository preferencias) {
        var servicio = new ListarPreferenciasService(preferencias);
        var transaccion = new TransactionTemplate(manager);
        transaccion.setReadOnly(true);
        return entrada -> transaccion.execute(status -> servicio.ejecutar(entrada));
    }

    @Bean
    public ActualizarPreferenciaUseCase actualizarPreferencia(
            PlatformTransactionManager manager, PreferenciaRepository preferencias) {
        var servicio = new ActualizarPreferenciaService(preferencias);
        var transaccion = new TransactionTemplate(manager);
        return entrada -> transaccion.execute(status -> servicio.ejecutar(entrada));
    }

    @Bean
    public ConsultarHistorialUseCase historial(
            PlatformTransactionManager manager, HistorialServiciosPort servicios) {
        var servicio = new ConsultarHistorialService(servicios);
        var transaccion = new TransactionTemplate(manager);
        transaccion.setReadOnly(true);
        return entrada -> transaccion.execute(status -> servicio.ejecutar(entrada));
    }

    @Bean
    public ExportarHistorialUseCase exportarHistorial(
            PlatformTransactionManager manager, HistorialServiciosPort servicios, DescargaRepository descargas, ArchivoHistorialPort storage) {
        var servicio = new ExportarHistorialService(servicios, descargas, storage);
        var transaccion = new TransactionTemplate(manager);
        transaccion.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_REPEATABLE_READ);
        return entrada -> transaccion.execute(status -> servicio.ejecutar(entrada));
    }

    @Bean
    public DescargarHistorialUseCase descargarExportacion(
            PlatformTransactionManager manager, DescargaRepository descargas, ArchivoHistorialPort storage) {
        var servicio = new DescargarHistorialService(descargas, storage);
        var transaccion = new TransactionTemplate(manager);
        transaccion.setReadOnly(true);
        return entrada -> transaccion.execute(status -> servicio.ejecutar(entrada));
    }

    @Bean
    public ConsultarIndicadoresPrestadorUseCase misIndicadores(
            PlatformTransactionManager manager, HistorialServiciosPort servicios, IndicadoresPrestadorPort indicadores) {
        var servicio = new ConsultarIndicadoresPrestadorService(servicios, indicadores);
        var transaccion = new TransactionTemplate(manager);
        transaccion.setReadOnly(true);
        return entrada -> transaccion.execute(status -> servicio.ejecutar(entrada));
    }

    @Bean
    public ConsultarActividadRecienteUseCase actividadReciente(
            PlatformTransactionManager manager, HistorialServiciosPort servicios, ActividadNotificacionesPort notificaciones) {
        var servicio = new ConsultarActividadRecienteService(servicios, notificaciones);
        var transaccion = new TransactionTemplate(manager);
        transaccion.setReadOnly(true);
        return entrada -> transaccion.execute(status -> servicio.ejecutar(entrada));
    }
}
