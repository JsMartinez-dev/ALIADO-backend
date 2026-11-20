package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.infrastructure.config;

import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service.CalcularCompatibilidadService;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.CalcularCompatibilidadUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.ConsultaOportunidadesPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service.EvaluacionCompatibilidad;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service.ProyeccionOportunidad;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service.ConsultarTableroOportunidadesService;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.ConsultarTableroOportunidadesUseCase;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service.FiltrarTableroOportunidadesService;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.FiltrarTableroOportunidadesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service.ConsultarDetalleOportunidadService;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.ConsultarDetalleOportunidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.DetectarNuevaOportunidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service.DetectarNuevaOportunidadService;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.RegistroNotificacionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.CompatibilidadPort;

@Configuration
public class OportunidadUseCaseConfig {
    @Bean
    public DetectarNuevaOportunidadUseCase detectarNuevaOportunidadUseCase(
            CompatibilidadPort compatibilidad, RegistroNotificacionPort registro,
            PlatformTransactionManager transactionManager) {
        var service = new DetectarNuevaOportunidadService(compatibilidad, registro);
        var transaction = new TransactionTemplate(transactionManager);
        return solicitudId -> transaction.executeWithoutResult(status -> service.ejecutar(solicitudId));
    }
    @Bean
    public ConsultarDetalleOportunidadUseCase consultarDetalleOportunidadUseCase(
            ConsultaOportunidadesPort consultas, EvaluacionCompatibilidad evaluacion,
            ProyeccionOportunidad proyeccion, PlatformTransactionManager transactionManager) {
        var service = new ConsultarDetalleOportunidadService(consultas, evaluacion, proyeccion);
        var transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        return query -> transaction.execute(status -> service.ejecutar(query));
    }
    @Bean
    public FiltrarTableroOportunidadesUseCase filtrarTableroOportunidadesUseCase(ConsultaOportunidadesPort consultas) {
        return new FiltrarTableroOportunidadesService(consultas);
    }
    @Bean
    public EvaluacionCompatibilidad evaluacionCompatibilidad(ConsultaOportunidadesPort consultas,
                                                             CalcularCompatibilidadUseCase calculo) {
        return new EvaluacionCompatibilidad(consultas, calculo);
    }

    @Bean
    public ProyeccionOportunidad proyeccionOportunidad(ConsultaOportunidadesPort consultas) {
        return new ProyeccionOportunidad(consultas);
    }

    @Bean
    public ConsultarTableroOportunidadesUseCase consultarTableroOportunidadesUseCase(
            ConsultaOportunidadesPort consultas, EvaluacionCompatibilidad evaluacion,
            ProyeccionOportunidad proyeccion, FiltrarTableroOportunidadesUseCase filtros,
            PlatformTransactionManager transactionManager) {
        var service = new ConsultarTableroOportunidadesService(consultas, evaluacion, proyeccion, filtros);
        var transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        return query -> transaction.execute(status -> service.ejecutar(query));
    }

    @Bean
    public CalcularCompatibilidadUseCase calcularCompatibilidadUseCase() {
        return new CalcularCompatibilidadService();
    }
}
