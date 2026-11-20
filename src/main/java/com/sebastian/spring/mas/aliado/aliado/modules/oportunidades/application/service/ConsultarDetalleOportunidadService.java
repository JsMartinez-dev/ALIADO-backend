package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.ConsultarDetalleOportunidadQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.OportunidadResult;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.exception.OportunidadNoDisponibleException;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.ConsultaOportunidadesPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.ConsultarDetalleOportunidadUseCase;
public class ConsultarDetalleOportunidadService implements ConsultarDetalleOportunidadUseCase {
 private final ConsultaOportunidadesPort consultas;
 private final EvaluacionCompatibilidad evaluacion;
 private final ProyeccionOportunidad proyeccion;
 public ConsultarDetalleOportunidadService(ConsultaOportunidadesPort consultas, EvaluacionCompatibilidad evaluacion,
                                          ProyeccionOportunidad proyeccion) {
  this.consultas=consultas; this.evaluacion=evaluacion; this.proyeccion=proyeccion;
 }
 @Override public OportunidadResult ejecutar(ConsultarDetalleOportunidadQuery query) {
  var perfil=consultas.perfilPorPrestadorId(query.prestadorId()).orElse(null);
  var solicitud=consultas.solicitudPorId(query.solicitudId()).orElse(null);
  if(perfil==null || solicitud==null || !evaluacion.compatible(solicitud,perfil)) {throw new OportunidadNoDisponibleException();}
  return proyeccion.proyectar(solicitud);
 }
}
