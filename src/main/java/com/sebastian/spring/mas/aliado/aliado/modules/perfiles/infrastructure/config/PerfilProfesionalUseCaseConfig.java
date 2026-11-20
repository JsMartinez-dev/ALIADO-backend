package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.infrastructure.config;

import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ListarCompromisosAgendaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.EliminarServicioPortafolioUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.CrearCompromisoAgendaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ConsultarVistaPreviaPerfilUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ConfigurarDisponibilidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.AgregarServicioPortafolioUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ActualizarCompromisoAgendaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service.ListarCompromisosAgendaService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service.GuardadoCompromisoAgenda;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service.EliminarServicioPortafolioService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service.CrearCompromisoAgendaService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service.ConsultarVistaPreviaPerfilService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service.ConfigurarDisponibilidadService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service.AgregarServicioPortafolioService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service.ActualizarCompromisoAgendaService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.ConsultaVistaPublicaPerfilPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.ArchivoPortafolioPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.RegistrarExperienciaProfesionalUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.EditarPerfilProfesionalUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ActualizarZonasAtencionUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ActualizarCategoriasProfesionalesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service.RegistrarExperienciaProfesionalService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service.EditarPerfilProfesionalService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service.ActualizarZonasAtencionService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service.ActualizarCategoriasProfesionalesService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.ConsultaCatalogoPerfilPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service.CrearPerfilProfesionalService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.CrearPerfilProfesionalUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class PerfilProfesionalUseCaseConfig {
    @Bean
    public ConfigurarDisponibilidadUseCase configurarDisponibilidadUseCase(
            PerfilProfesionalRepository perfiles, PlatformTransactionManager transactionManager) {
        var service = new ConfigurarDisponibilidadService(perfiles);
        var transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public CrearCompromisoAgendaUseCase crearCompromisoAgendaUseCase(
            GuardadoCompromisoAgenda guardado, PlatformTransactionManager transactionManager) {
        var service = new CrearCompromisoAgendaService(guardado);
        var transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public ActualizarCompromisoAgendaUseCase actualizarCompromisoAgendaUseCase(
            GuardadoCompromisoAgenda guardado, PlatformTransactionManager transactionManager) {
        var service = new ActualizarCompromisoAgendaService(guardado);
        var transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public ListarCompromisosAgendaUseCase listarCompromisosAgendaUseCase(
            PerfilProfesionalRepository perfiles, PlatformTransactionManager transactionManager) {
        var service = new ListarCompromisosAgendaService(perfiles);
        var transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public ConsultarVistaPreviaPerfilUseCase consultarVistaPreviaPerfilUseCase(
            ConsultaVistaPublicaPerfilPort consulta, PlatformTransactionManager transactionManager) {
        var service = new ConsultarVistaPreviaPerfilService(consulta);
        var transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public GuardadoCompromisoAgenda guardadoCompromisoAgenda(PerfilProfesionalRepository perfiles) {
        return new GuardadoCompromisoAgenda(perfiles);
    }

    @Bean
    public EliminarServicioPortafolioUseCase eliminarServicioPortafolioUseCase(
            PerfilProfesionalRepository perfiles, ArchivoPortafolioPort archivos,
            PlatformTransactionManager transactionManager) {
        var service = new EliminarServicioPortafolioService(perfiles, archivos);
        var transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.executeWithoutResult(status -> service.ejecutar(command));
    }

    @Bean
    public AgregarServicioPortafolioUseCase agregarServicioPortafolioUseCase(
            PerfilProfesionalRepository perfiles, ArchivoPortafolioPort archivos,
            PlatformTransactionManager transactionManager) {
        var service = new AgregarServicioPortafolioService(perfiles, archivos);
        var transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public EditarPerfilProfesionalUseCase editarPerfilProfesionalUseCase(
            PerfilProfesionalRepository perfiles, PlatformTransactionManager transactionManager) {
        var service = new EditarPerfilProfesionalService(perfiles);
        var transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public RegistrarExperienciaProfesionalUseCase registrarExperienciaProfesionalUseCase(
            PerfilProfesionalRepository perfiles, PlatformTransactionManager transactionManager) {
        var service = new RegistrarExperienciaProfesionalService(perfiles);
        var transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public ActualizarCategoriasProfesionalesUseCase actualizarCategoriasProfesionalesUseCase(
            PerfilProfesionalRepository perfiles, ConsultaCatalogoPerfilPort catalogo, PlatformTransactionManager transactionManager) {
        var service = new ActualizarCategoriasProfesionalesService(perfiles, catalogo);
        var transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public ActualizarZonasAtencionUseCase actualizarZonasAtencionUseCase(
            PerfilProfesionalRepository perfiles, ConsultaCatalogoPerfilPort catalogo, PlatformTransactionManager transactionManager) {
        var service = new ActualizarZonasAtencionService(perfiles, catalogo);
        var transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public CrearPerfilProfesionalUseCase crearPerfilProfesionalUseCase(PerfilProfesionalRepository perfiles,
            ConsultaCatalogoPerfilPort catalogo, PlatformTransactionManager transactionManager) {
        CrearPerfilProfesionalService service = new CrearPerfilProfesionalService(perfiles, catalogo);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }
}
