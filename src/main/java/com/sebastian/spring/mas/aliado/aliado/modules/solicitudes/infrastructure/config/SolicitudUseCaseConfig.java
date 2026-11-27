package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.infrastructure.config;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.ConsultaZonaSolicitudPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.ConsultaCategoriaSolicitudPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service.CrearSolicitudService;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service.CreacionSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service.GuardarBorradorService;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.GuardarBorradorUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service.PublicarSolicitudService;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.CrearSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.PublicarSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.repository.SolicitudRepository;
import com.sebastian.spring.mas.aliado.aliado.common.events.PublicadorEventos;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service.DefinirZonaUrgenciaService;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.DefinirZonaUrgenciaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.ArchivoFotoSolicitudPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.AccesoFotoOportunidadPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service.AgregarFotoSolicitudService;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.AgregarFotoSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service.ReemplazarFotoSolicitudService;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.ReemplazarFotoSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service.EliminarFotoSolicitudService;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.EliminarFotoSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service.DescargarFotoSolicitudService;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.DescargarFotoSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service.EditarSolicitudService;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.EditarSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service.CancelarSolicitudService;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.CancelarSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service.ListarSolicitudesPropiasService;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.ListarSolicitudesPropiasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service.ConsultarSolicitudPropiaService;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.ConsultarSolicitudPropiaUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class SolicitudUseCaseConfig {
    @Bean
    public ConsultarSolicitudPropiaUseCase consultarSolicitudPropiaUseCase(SolicitudRepository solicitudes,
            PlatformTransactionManager transactionManager) {
        ConsultarSolicitudPropiaService service = new ConsultarSolicitudPropiaService(solicitudes);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        return query -> transaction.execute(status -> service.ejecutar(query));
    }

    @Bean
    public ListarSolicitudesPropiasUseCase listarSolicitudesPropiasUseCase(SolicitudRepository solicitudes,
            PlatformTransactionManager transactionManager) {
        ListarSolicitudesPropiasService service = new ListarSolicitudesPropiasService(solicitudes);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        return clienteId -> transaction.execute(status -> service.ejecutar(clienteId));
    }

    @Bean
    public CancelarSolicitudUseCase cancelarSolicitudUseCase(SolicitudRepository solicitudes,
            PlatformTransactionManager transactionManager) {
        CancelarSolicitudService service = new CancelarSolicitudService(solicitudes);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public EditarSolicitudUseCase editarSolicitudUseCase(SolicitudRepository solicitudes,
            ConsultaCategoriaSolicitudPort categorias, ConsultaZonaSolicitudPort zonas,
            PlatformTransactionManager transactionManager) {
        EditarSolicitudService service = new EditarSolicitudService(solicitudes, categorias, zonas);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public AgregarFotoSolicitudUseCase agregarFotoSolicitudUseCase(SolicitudRepository solicitudes,
            ArchivoFotoSolicitudPort archivos, PlatformTransactionManager transactionManager) {
        AgregarFotoSolicitudService service = new AgregarFotoSolicitudService(solicitudes, archivos);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public ReemplazarFotoSolicitudUseCase reemplazarFotoSolicitudUseCase(SolicitudRepository solicitudes,
            ArchivoFotoSolicitudPort archivos, PlatformTransactionManager transactionManager) {
        ReemplazarFotoSolicitudService service = new ReemplazarFotoSolicitudService(solicitudes, archivos);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public EliminarFotoSolicitudUseCase eliminarFotoSolicitudUseCase(SolicitudRepository solicitudes,
            ArchivoFotoSolicitudPort archivos, PlatformTransactionManager transactionManager) {
        EliminarFotoSolicitudService service = new EliminarFotoSolicitudService(solicitudes, archivos);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.executeWithoutResult(status -> service.ejecutar(command));
    }

    @Bean
    public DescargarFotoSolicitudUseCase descargarFotoSolicitudUseCase(SolicitudRepository solicitudes,
            ArchivoFotoSolicitudPort archivos, AccesoFotoOportunidadPort accesoOportunidad,
            PlatformTransactionManager transactionManager) {
        DescargarFotoSolicitudService service = new DescargarFotoSolicitudService(solicitudes, archivos, accesoOportunidad);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }


    @Bean
    public CreacionSolicitud creacionSolicitud(SolicitudRepository solicitudes,
            ConsultaCategoriaSolicitudPort categorias, DefinirZonaUrgenciaUseCase zonaUrgencia,
            PublicadorEventos eventos) {
        return new CreacionSolicitud(solicitudes, categorias, zonaUrgencia, eventos);
    }

    @Bean
    public GuardarBorradorUseCase guardarBorradorUseCase(CreacionSolicitud creacion,
            PlatformTransactionManager transactionManager) {
        GuardarBorradorService service = new GuardarBorradorService(creacion);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public CrearSolicitudUseCase crearSolicitudUseCase(CreacionSolicitud creacion,
            GuardarBorradorUseCase guardarBorrador, PlatformTransactionManager transactionManager) {
        CrearSolicitudService service = new CrearSolicitudService(creacion, guardarBorrador);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public PublicarSolicitudUseCase publicarSolicitudUseCase(SolicitudRepository solicitudes,
            ConsultaCategoriaSolicitudPort categorias, DefinirZonaUrgenciaUseCase zonaUrgencia,
            PublicadorEventos eventos, PlatformTransactionManager transactionManager) {
        PublicarSolicitudService service = new PublicarSolicitudService(solicitudes, categorias, zonaUrgencia, eventos);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.ejecutar(command));
    }

    @Bean
    public DefinirZonaUrgenciaUseCase definirZonaUrgenciaUseCase(ConsultaZonaSolicitudPort zonas) {
        return new DefinirZonaUrgenciaService(zonas);
    }
}
