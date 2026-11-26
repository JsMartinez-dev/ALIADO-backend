package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.ConsultarDetalleOportunidadQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.OportunidadResult;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.exception.OportunidadNoDisponibleException;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.ConsultaOportunidadesPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.ConsultarDetalleOportunidadUseCase;
public class ConsultarDetalleOportunidadService implements ConsultarDetalleOportunidadUseCase {
 private final ConsultaOportunidadesPort consultaOportunidadesPort;
 private final EvaluacionCompatibilidad evaluacionCompatibilidad;
 private final ProyeccionOportunidad proyeccionOportunidad;
 public ConsultarDetalleOportunidadService(ConsultaOportunidadesPort consultas, EvaluacionCompatibilidad evaluacion,
                                          ProyeccionOportunidad proyeccionOportunidad) {
  this.consultaOportunidadesPort =consultas; this.evaluacionCompatibilidad =evaluacion; this.proyeccionOportunidad = proyeccionOportunidad;
 }

 @Override public OportunidadResult ejecutar(ConsultarDetalleOportunidadQuery query) {
  var perfil= consultaOportunidadesPort.perfilPorPrestadorId(query.prestadorId()).orElse(null);
  var solicitud= consultaOportunidadesPort.solicitudPorId(query.solicitudId()).orElse(null);

  if(perfil==null || solicitud==null || !evaluacionCompatibilidad.compatible(solicitud,perfil)) {
    throw new OportunidadNoDisponibleException();
  }

  return proyeccionOportunidad.proyectar(solicitud);
 }
}
