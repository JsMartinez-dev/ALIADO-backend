package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.repository.ServicioRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.exception.ServicioOperacionException;

public class ConsultarServicioService implements ConsultarServicioUseCase {
 private final ServicioRepository servicioRepository;
 private final ConsultaGestionSolicitudPort consultaGestionSolicitudPort;
 private final ConsultaGestionPropuestasPort consultaGestionPropuestasPort;
 public ConsultarServicioService(ServicioRepository r,ConsultaGestionSolicitudPort s,ConsultaGestionPropuestasPort p){
  servicioRepository =r;
  consultaGestionSolicitudPort =s;
  consultaGestionPropuestasPort =p;}
 public ServicioResult ejecutar(AccionServicioCommand command){
  var s=AccesoServicio.participante(servicioRepository,command);

 var sol= consultaGestionSolicitudPort.porId(s.solicitudId())
         .orElseThrow(()->AccesoServicio.error(ServicioOperacionException.Tipo.NO_ENCONTRADO,"SOLICITUD_NO_ENCONTRADA","Solicitud no encontrada."));
 var p= consultaGestionPropuestasPort.porId(s.propuestaId())
         .orElseThrow(()->AccesoServicio.error(ServicioOperacionException.Tipo.NO_ENCONTRADO,"PROPUESTA_NO_ENCONTRADA","Propuesta no encontrada."));

 return new ServicioResult(s.id(),s.solicitudId(),s.propuestaId(),s.clienteId(),s.prestadorId(),
            s.estado().name(),s.comunicacionHabilitada(),s.fechaContratacion(),
            new ServicioResult.SolicitudResumen(sol.id(),sol.descripcion(),sol.categoriaId(),
                    sol.zonaId(),sol.urgencia()==null?null:sol.urgencia().name(),sol.estado().name())
            ,new ServicioResult.PropuestaResumen(p.id(),p.prestadorId(),p.disponibilidad(),
            p.mensaje(),p.estado().name(),p.fechaCreacion()));}
}
