package com.sebastian.spring.mas.aliado.aliado.modules.administracion.infrastructure.config;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.domain.repository.AccionModeracionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.domain.repository.AuditoriaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.domain.repository.BloqueoRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.domain.repository.MarcaRiesgoRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.CuentaBloqueoPort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.UsuarioConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoAdminPort;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ResenaModeracionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.VerificacionAdminPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.PerfilBusquedaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.PerfilModeracionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.PerfilProfesionalModeracionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.port.ReporteAdminPort;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.ServicioLecturaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.SolicitudConsultaPort;
import java.time.*;
import java.util.*;
import org.springframework.context.annotation.*;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class AdminUseCaseConfig {
  @Bean
  public ReporteModeracionService reporteModeracionService(
      ReporteAdminPort reportes,
      com.sebastian.spring.mas.aliado.aliado.common.auditoria.AuditoriaPort auditoriaService) {
    return new ReporteModeracionService(reportes, auditoriaService);
  }

  @Bean
  public BloqueoCuentaService bloqueoCuentaService(
      BloqueoRepository bloqueos,
      com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.port
              .ConsultaEliminacionCuentaPort
          eliminaciones,
      UsuarioConsultaPort usuarios,
      CuentaBloqueoPort cuentas,
      com.sebastian.spring.mas.aliado.aliado.common.auditoria.AuditoriaPort auditoriaService) {
    return new BloqueoCuentaService(bloqueos, eliminaciones, usuarios, cuentas, auditoriaService);
  }

  @Bean
  public ModeracionService moderacionService(
      AccionModeracionRepository acciones,
      ResenaModeracionPort resenas,
      VerificacionAdminPort verificaciones,
      com.sebastian.spring.mas.aliado.aliado.common.auditoria.AuditoriaPort auditoriaService,
      PerfilProfesionalModeracionPort perfilesProfesionales,
      PerfilModeracionPort perfiles) {
    return new ModeracionService(
        acciones, resenas, verificaciones, auditoriaService, perfilesProfesionales, perfiles);
  }

  @Bean
  public BusquedaAdminService busquedaAdminService(
      UsuarioConsultaPort usuarios,
      PerfilBusquedaPort perfiles,
      SolicitudConsultaPort solicitudes) {
    return new BusquedaAdminService(usuarios, perfiles, solicitudes);
  }

  @Bean
  public RiesgoAuditoriaService riesgoAuditoriaService(
      MarcaRiesgoRepository marcas,
      ServicioLecturaPort servicios,
      AuditoriaRepository auditoria,
      com.sebastian.spring.mas.aliado.aliado.common.auditoria.AuditoriaPort auditoriaService) {
    return new RiesgoAuditoriaService(marcas, servicios, auditoria, auditoriaService);
  }

  @Bean
  public CrearCategoriaUseCase crearCategoriaUseCase(
      CatalogoAdminPort operaciones, PlatformTransactionManager manager) {
    var service = new CrearCategoriaService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(false);
    return command -> transaction.execute(status -> service.ejecutar(command));
  }

  @Bean
  public ListarCategoriasUseCase listarCategoriasUseCase(
      CatalogoAdminPort operaciones, PlatformTransactionManager manager) {
    var service = new ListarCategoriasService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(true);
    return () -> transaction.execute(status -> service.ejecutar());
  }

  @Bean
  public EditarCategoriaUseCase editarCategoriaUseCase(
      CatalogoAdminPort operaciones, PlatformTransactionManager manager) {
    var service = new EditarCategoriaService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(false);
    return command -> transaction.execute(status -> service.ejecutar(command));
  }

  @Bean
  public DesactivarCategoriaUseCase desactivarCategoriaUseCase(
      CatalogoAdminPort operaciones, PlatformTransactionManager manager) {
    var service = new DesactivarCategoriaService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(false);
    return command -> transaction.executeWithoutResult(status -> service.ejecutar(command));
  }

  @Bean
  public CrearZonaUseCase crearZonaUseCase(
      CatalogoAdminPort operaciones, PlatformTransactionManager manager) {
    var service = new CrearZonaService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(false);
    return command -> transaction.execute(status -> service.ejecutar(command));
  }

  @Bean
  public ListarZonasUseCase listarZonasUseCase(
      CatalogoAdminPort operaciones, PlatformTransactionManager manager) {
    var service = new ListarZonasService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(true);
    return () -> transaction.execute(status -> service.ejecutar());
  }

  @Bean
  public EditarZonaUseCase editarZonaUseCase(
      CatalogoAdminPort operaciones, PlatformTransactionManager manager) {
    var service = new EditarZonaService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(false);
    return command -> transaction.execute(status -> service.ejecutar(command));
  }

  @Bean
  public DesactivarZonaUseCase desactivarZonaUseCase(
      CatalogoAdminPort operaciones, PlatformTransactionManager manager) {
    var service = new DesactivarZonaService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(false);
    return command -> transaction.executeWithoutResult(status -> service.ejecutar(command));
  }

  @Bean
  public ConsultarColaReportesUseCase consultarColaReportesUseCase(
      ReporteModeracionService operaciones, PlatformTransactionManager manager) {
    var service = new ConsultarColaReportesService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(true);
    return command -> transaction.execute(status -> service.ejecutar(command));
  }

  @Bean
  public RevisarReporteUseCase revisarReporteUseCase(
      ReporteModeracionService operaciones, PlatformTransactionManager manager) {
    var service = new RevisarReporteService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(false);
    return command -> transaction.execute(status -> service.ejecutar(command));
  }

  @Bean
  public ResolverReporteUseCase resolverReporteUseCase(
      ReporteModeracionService operaciones, PlatformTransactionManager manager) {
    var service = new ResolverReporteService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(false);
    return command -> transaction.execute(status -> service.ejecutar(command));
  }

  @Bean
  public DescargarEvidenciaAdministrativaUseCase descargarEvidenciaAdministrativaUseCase(
      ReporteModeracionService operaciones, PlatformTransactionManager manager) {
    var service = new DescargarEvidenciaAdministrativaService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(true);
    return command -> transaction.execute(status -> service.ejecutar(command));
  }

  @Bean
  public BloquearCuentaUseCase bloquearCuentaUseCase(
      BloqueoCuentaService operaciones, PlatformTransactionManager manager) {
    var service = new BloquearCuentaService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(false);
    return command -> transaction.execute(status -> service.ejecutar(command));
  }

  @Bean
  public LevantarBloqueoUseCase levantarBloqueoUseCase(
      BloqueoCuentaService operaciones, PlatformTransactionManager manager) {
    var service = new LevantarBloqueoService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(false);
    return command -> transaction.executeWithoutResult(status -> service.ejecutar(command));
  }

  @Bean
  public ModerarContenidoUseCase moderarContenidoUseCase(
      ModeracionService operaciones, PlatformTransactionManager manager) {
    var service = new ModerarContenidoService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(false);
    return command -> transaction.executeWithoutResult(status -> service.ejecutar(command));
  }

  @Bean
  public ResolverVerificacionAdministrativaUseCase resolverVerificacionAdministrativaUseCase(
      ModeracionService operaciones, PlatformTransactionManager manager) {
    var service = new ResolverVerificacionAdministrativaService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(false);
    return command -> transaction.executeWithoutResult(status -> service.ejecutar(command));
  }

  @Bean
  public BuscarCuentasPerfilesUseCase buscarCuentasPerfilesUseCase(
      BusquedaAdminService operaciones, PlatformTransactionManager manager) {
    var service = new BuscarCuentasPerfilesService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(true);
    return command -> transaction.execute(status -> service.ejecutar(command));
  }

  @Bean
  public MarcarServicioAltoRiesgoUseCase marcarServicioAltoRiesgoUseCase(
      RiesgoAuditoriaService operaciones, PlatformTransactionManager manager) {
    var service = new MarcarServicioAltoRiesgoService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(false);
    return command -> transaction.execute(status -> service.ejecutar(command));
  }

  @Bean
  public ConsultarAuditoriaUseCase consultarAuditoriaUseCase(
      RiesgoAuditoriaService operaciones, PlatformTransactionManager manager) {
    var service = new ConsultarAuditoriaService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(true);
    return command -> transaction.execute(status -> service.ejecutar(command));
  }
}
