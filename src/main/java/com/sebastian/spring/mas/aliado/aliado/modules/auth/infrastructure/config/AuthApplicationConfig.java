package com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.config;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.HasheadorPassword;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.GeneradorTokenSesion;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.HuellaToken;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service.IniciarSesionService;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service.CerrarSesionService;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase.CerrarSesionUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase.IniciarSesionUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.PerfilBasePort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service.RegistrarUsuarioService;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service.RegistrarConsentimientoService;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase.RegistrarConsentimientoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase.RegistrarUsuarioUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service.SolicitarRecuperacionAccesoService;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service.ConfirmarRecuperacionAccesoService;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service.AuthService;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase.SolicitarRecuperacionAccesoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase.ConfirmarRecuperacionAccesoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.ConsentimientoRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.UsuarioRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.SesionRepository;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Composición técnica de los casos de uso del módulo auth. */
@Configuration
public class AuthApplicationConfig {

    @Bean
    public SolicitarRecuperacionAccesoUseCase solicitarRecuperacionAccesoUseCase(AuthService authService) {
        return new SolicitarRecuperacionAccesoService(authService);
    }

    @Bean
    public ConfirmarRecuperacionAccesoUseCase confirmarRecuperacionAccesoUseCase(AuthService authService) {
        return new ConfirmarRecuperacionAccesoService(authService);
    }

    @Bean
    public CerrarSesionUseCase cerrarSesionUseCase(com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.SesionPort sesiones,
                                                    PlatformTransactionManager transactionManager) {
        CerrarSesionService service = new CerrarSesionService(sesiones);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return token -> transaction.executeWithoutResult(status -> service.ejecutar(token));
    }

    @Bean
    public IniciarSesionService iniciarSesionService(UsuarioRepository usuarios, SesionRepository sesiones,
                                                     HasheadorPassword passwords, GeneradorTokenSesion generador,
                                                     HuellaToken huella,
                                                     @Value("${aliado.session.ttl-hours:24}") int ttlHoras) {
        return new IniciarSesionService(usuarios, sesiones, passwords, generador, huella,
                Clock.systemUTC(), Duration.ofHours(ttlHoras));
    }

    @Bean
    @Primary
    public IniciarSesionUseCase iniciarSesionUseCase(IniciarSesionService service,
                                                     PlatformTransactionManager transactionManager) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public RegistrarConsentimientoUseCase registrarConsentimientoUseCase(
            ConsentimientoRepository consentimientos) {
        return new RegistrarConsentimientoService(consentimientos);
    }

    @Bean
    public RegistrarUsuarioService registrarUsuarioService(UsuarioRepository usuarios,
                                                           RegistrarConsentimientoUseCase registrarConsentimientoUseCase,
                                                           PerfilBasePort perfilBase,
                                                           HasheadorPassword hasheadorPassword) {
        return new RegistrarUsuarioService(usuarios, registrarConsentimientoUseCase, perfilBase, hasheadorPassword);
    }

    @Bean
    @Primary
    public RegistrarUsuarioUseCase registrarUsuarioUseCase(RegistrarUsuarioService service,
                                                            PlatformTransactionManager transactionManager) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }
}
