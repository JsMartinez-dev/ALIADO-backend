package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.ConsultarTableroQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.OportunidadResult;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.ConsultaOportunidadesPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.ConsultarTableroOportunidadesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.EstadoExposicion;
import java.util.List;
import java.util.UUID;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.FiltrarTableroCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.FiltrarTableroOportunidadesUseCase;
public class ConsultarTableroOportunidadesService implements ConsultarTableroOportunidadesUseCase {
 private final ConsultaOportunidadesPort consultas;
 private final EvaluacionCompatibilidad evaluacion;
 private final ProyeccionOportunidad proyeccion;
 private final FiltrarTableroOportunidadesUseCase filtros;
 public ConsultarTableroOportunidadesService(ConsultaOportunidadesPort consultas, EvaluacionCompatibilidad evaluacion,
                                            ProyeccionOportunidad proyeccion, FiltrarTableroOportunidadesUseCase filtros) {
  this.consultas=consultas; this.evaluacion=evaluacion; this.proyeccion=proyeccion;
  this.filtros=filtros;
 }
 @Override public List<OportunidadResult> ejecutar(ConsultarTableroQuery query) {
  var perfil=consultas.perfilPorPrestadorId(query.prestadorId()).orElse(null);
  if(perfil==null || perfil.estadoExposicion()!=EstadoExposicion.PUBLICO) {return List.of();}
  var compatibles=consultas.publicadas().stream().filter(s->evaluacion.compatible(s,perfil)).toList();
  return filtros.ejecutar(new FiltrarTableroCommand(compatibles,query.categoria(),query.urgencia())).stream()
   .map(proyeccion::proyectar).toList();
 }
}
