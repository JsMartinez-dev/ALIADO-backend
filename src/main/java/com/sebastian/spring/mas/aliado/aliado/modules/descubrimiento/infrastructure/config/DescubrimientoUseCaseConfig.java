package com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.infrastructure.config;

import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.port.ConsultaDescubrimientoPort;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.service.BuscarPrestadoresService;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.service.ProyeccionPrestadorPublico;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.usecase.BuscarPrestadoresUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.usecase.ConsultarPerfilPublicoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.service.ConsultarPerfilPublicoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class DescubrimientoUseCaseConfig {
    @Bean
    public ConsultarPerfilPublicoUseCase consultarPerfilPublicoPrestadorUseCase(ConsultaDescubrimientoPort consultas,
                                                                             ProyeccionPrestadorPublico proyeccion,
                                                                             PlatformTransactionManager transactionManager) {
        ConsultarPerfilPublicoService service = new ConsultarPerfilPublicoService(consultas, proyeccion);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        return prestadorId -> transaction.execute(status -> service.ejecutar(prestadorId));
    }

    @Bean
    public ProyeccionPrestadorPublico proyeccionPrestadorPublico(ConsultaDescubrimientoPort consultas) {
        return new ProyeccionPrestadorPublico(consultas);
    }

    @Bean
    public BuscarPrestadoresUseCase buscarPrestadoresUseCase(ConsultaDescubrimientoPort consultas,
                                                           ProyeccionPrestadorPublico proyeccion,
                                                           PlatformTransactionManager transactionManager) {
        BuscarPrestadoresService service = new BuscarPrestadoresService(consultas, proyeccion);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        return query -> transaction.execute(status -> service.ejecutar(query));
    }
}

