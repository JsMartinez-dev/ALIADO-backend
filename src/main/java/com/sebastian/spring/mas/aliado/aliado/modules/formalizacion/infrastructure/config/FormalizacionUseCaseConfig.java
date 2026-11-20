package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.infrastructure.config;

import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.service.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.domain.repository.*;
import org.springframework.context.annotation.*;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class FormalizacionUseCaseConfig {
  @Bean
  public FormalizacionService formalizacionService(
      RutaRepository rutas,
      PasoRepository pasos,
      EnlaceRepository enlaces,
      ProgresoRepository progresos) {
    return new FormalizacionService(rutas, pasos, enlaces, progresos);
  }

  @Bean
  public ConsultarRutaFormalizacionUseCase consultarRutaFormalizacionUseCase(
      FormalizacionService operaciones, PlatformTransactionManager manager) {
    var service = new ConsultarRutaFormalizacionService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(true);
    return () -> transaction.execute(status -> service.ejecutar());
  }

  @Bean
  public ConsultarEnlaceInstitucionalUseCase consultarEnlaceInstitucionalUseCase(
      FormalizacionService operaciones, PlatformTransactionManager manager) {
    var service = new ConsultarEnlaceInstitucionalService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(true);
    return command -> transaction.execute(status -> service.ejecutar(command));
  }

  @Bean
  public ConsultarProgresoFormalizacionUseCase consultarProgresoFormalizacionUseCase(
      FormalizacionService operaciones, PlatformTransactionManager manager) {
    var service = new ConsultarProgresoFormalizacionService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(true);
    return command -> transaction.execute(status -> service.ejecutar(command));
  }

  @Bean
  public ActualizarProgresoFormalizacionUseCase actualizarProgresoFormalizacionUseCase(
      FormalizacionService operaciones, PlatformTransactionManager manager) {
    var service = new ActualizarProgresoFormalizacionService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(false);
    return command -> transaction.execute(status -> service.ejecutar(command));
  }

  @Bean
  public ActualizarEnlaceInstitucionalUseCase actualizarEnlaceInstitucionalUseCase(
      FormalizacionService operaciones, PlatformTransactionManager manager) {
    var service = new ActualizarEnlaceInstitucionalService(operaciones);
    var transaction = new TransactionTemplate(manager);
    transaction.setReadOnly(false);
    return command -> transaction.execute(status -> service.ejecutar(command));
  }

  @Bean
  public ConsultarVinculacionInstitucionalUseCase consultarVinculacionInstitucionalUseCase(
      com.sebastian.spring.mas.aliado.aliado.config.FeatureFlags flags) {
    return new ConsultarVinculacionInstitucionalService(flags::integracionInstitucional);
  }
}
