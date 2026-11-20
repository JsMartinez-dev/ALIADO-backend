package com.sebastian.spring.mas.aliado.aliado.modules.perfil.infrastructure.config;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service.ConsultarPerfilService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service.ActualizarPerfilService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ActualizarPerfilUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConfigurarUbicacionUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service.ConfigurarUbicacionService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service.ConfigurarPrivacidadService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConfigurarPrivacidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConsultarPerfilPublicoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service.ConsultarPerfilPublicoService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service.SolicitarEliminacionCuentaService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service.ConfirmarEliminacionCuentaService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service.ExpirarSolicitudesEliminacionService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.SolicitarEliminacionCuentaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConfirmarEliminacionCuentaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ExpirarSolicitudesEliminacionUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.ConsultaServiciosActivosPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.SolicitudExpiradaException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.EstadoEliminacion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.SolicitudEliminacionRepository;
import com.sebastian.spring.mas.aliado.aliado.common.events.PublicadorEventos;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConsultarPerfilUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.PerfilRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.UbicacionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Composición técnica y transacción de lectura de UC011. */
@Configuration
public class PerfilUseCaseConfig {
    @Bean
    public SolicitarEliminacionCuentaUseCase solicitarEliminacionCuentaUseCase(SolicitudEliminacionRepository eliminaciones,
                                                                             ConsultaServiciosActivosPort servicios,
                                                                             PlatformTransactionManager transactionManager) {
        SolicitarEliminacionCuentaService service = new SolicitarEliminacionCuentaService(eliminaciones, servicios);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return usuarioId -> transaction.execute(status -> service.ejecutar(usuarioId));
    }

    @Bean
    public ConfirmarEliminacionCuentaUseCase confirmarEliminacionCuentaUseCase(SolicitudEliminacionRepository eliminaciones,
                                                                             ConsultaServiciosActivosPort servicios,
                                                                             PublicadorEventos eventos,
                                                                             PlatformTransactionManager transactionManager) {
        ConfirmarEliminacionCuentaService service = new ConfirmarEliminacionCuentaService(eliminaciones, servicios, eventos);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return usuarioId -> {
            var resultado = transaction.execute(status -> service.ejecutar(usuarioId));
            // La expiración debe quedar confirmada antes de traducirla al error de aplicación.
            if (resultado != null && EstadoEliminacion.EXPIRADA.name().equals(resultado.estado())) {
                throw new SolicitudExpiradaException();
            }
            return resultado;
        };
    }

    @Bean
    public ExpirarSolicitudesEliminacionUseCase expirarSolicitudesEliminacionUseCase(SolicitudEliminacionRepository eliminaciones,
                                                                                  PlatformTransactionManager transactionManager) {
        ExpirarSolicitudesEliminacionService service = new ExpirarSolicitudesEliminacionService(eliminaciones);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return () -> transaction.executeWithoutResult(status -> service.ejecutar());
    }

    @Bean
    public ConsultarPerfilPublicoUseCase consultarPerfilPublicoUseCase(PerfilRepository perfiles,
                                                                     UbicacionRepository ubicaciones,
                                                                     PlatformTransactionManager transactionManager) {
        ConsultarPerfilPublicoService service = new ConsultarPerfilPublicoService(perfiles, ubicaciones);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        return usuarioId -> transaction.execute(status -> service.ejecutar(usuarioId));
    }

    @Bean
    public ConfigurarPrivacidadUseCase configurarPrivacidadUseCase(PerfilRepository perfiles,
                                                                 ConsultarPerfilUseCase consultarPerfilUseCase,
                                                                 PlatformTransactionManager transactionManager) {
        ConfigurarPrivacidadService service = new ConfigurarPrivacidadService(perfiles, consultarPerfilUseCase);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public ConfigurarUbicacionUseCase configurarUbicacionUseCase(UbicacionRepository ubicaciones,
                                                               ConsultarPerfilUseCase consultarPerfilUseCase,
                                                               PlatformTransactionManager transactionManager) {
        ConfigurarUbicacionService service = new ConfigurarUbicacionService(ubicaciones, consultarPerfilUseCase);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public ActualizarPerfilUseCase actualizarPerfilUseCase(PerfilRepository perfiles,
                                                         ConsultarPerfilUseCase consultarPerfilUseCase,
                                                         PlatformTransactionManager transactionManager) {
        ActualizarPerfilService service = new ActualizarPerfilService(perfiles, consultarPerfilUseCase);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public ConsultarPerfilUseCase consultarPerfilUseCase(PerfilRepository perfiles,
                                                       UbicacionRepository ubicaciones,
                                                       PlatformTransactionManager transactionManager) {
        ConsultarPerfilService service = new ConsultarPerfilService(perfiles, ubicaciones);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        return usuarioId -> transaction.execute(status -> service.ejecutar(usuarioId));
    }
}
