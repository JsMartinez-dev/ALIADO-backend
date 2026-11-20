package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.ConsultaOportunidadesPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.CalcularCompatibilidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.domain.model.CriteriosCompatibilidad;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.EstadoExposicion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Visibilidad;
public class EvaluacionCompatibilidad {
 private final ConsultaOportunidadesPort consultas;
 private final CalcularCompatibilidadUseCase calculo;
 public EvaluacionCompatibilidad(ConsultaOportunidadesPort consultas, CalcularCompatibilidadUseCase calculo) {
  this.consultas=consultas; this.calculo=calculo;
 }
 public boolean compatible(Solicitud solicitud, PerfilProfesional perfil) {
  boolean publicada=solicitud.estado()==EstadoSolicitud.PUBLICADA;
  boolean publico=perfil.estadoExposicion()==EstadoExposicion.PUBLICO;
  boolean privado=publicada && publico && consultas.preferenciaDe(perfil.prestadorId())==Visibilidad.PRIVADO;
  return calculo.ejecutar(new CriteriosCompatibilidad(publicada,publico,privado,solicitud.categoriaId(),
    solicitud.zonaId(),perfil.categoriaIds(),perfil.zonaIds(),!perfil.disponibilidades().isEmpty()));
 }
}
