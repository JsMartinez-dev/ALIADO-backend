package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.ConsultarTableroQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.OportunidadResult;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.ConsultaOportunidadesPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.ConsultarTableroOportunidadesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.EstadoExposicion;
import java.util.List;

import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.FiltrarTableroCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.FiltrarTableroOportunidadesUseCase;
public class ConsultarTableroOportunidadesService implements ConsultarTableroOportunidadesUseCase {
 private final ConsultaOportunidadesPort consultaOportunidadesPort;
 private final EvaluacionCompatibilidad evaluacionCompatibilidad;
 private final ProyeccionOportunidad proyeccionOportunidad;
 private final FiltrarTableroOportunidadesUseCase tableroOportunidadesUseCase;
 public ConsultarTableroOportunidadesService(ConsultaOportunidadesPort consultas, EvaluacionCompatibilidad evaluacion,
                                            ProyeccionOportunidad proyeccion, FiltrarTableroOportunidadesUseCase filtros) {
  this.consultaOportunidadesPort =consultas; this.evaluacionCompatibilidad =evaluacion; this.proyeccionOportunidad =proyeccion;
  this.tableroOportunidadesUseCase =filtros;
 }
 @Override public List<OportunidadResult> ejecutar(ConsultarTableroQuery query) {
  var perfil= consultaOportunidadesPort.perfilPorPrestadorId(query.prestadorId()).orElse(null);

  if(perfil==null || perfil.estadoExposicion()!=EstadoExposicion.PUBLICO) {
     return List.of();
  }

  var compatibles= consultaOportunidadesPort.publicadas().stream()
          .filter(s-> evaluacionCompatibilidad.compatible(s,perfil)).toList();

  return tableroOportunidadesUseCase.ejecutar(new FiltrarTableroCommand(compatibles,query.categoria(),query.urgencia())).stream()
   .map(proyeccionOportunidad::proyectar).toList();
 }
}
