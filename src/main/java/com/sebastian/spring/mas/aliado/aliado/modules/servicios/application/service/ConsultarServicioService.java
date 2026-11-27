package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.service;

import java.util.*;
import java.time.OffsetDateTime;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.repository.ServicioRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.exception.ServicioOperacionException;
import com.sebastian.spring.mas.aliado.aliado.common.events.*;
public class ConsultarServicioService implements ConsultarServicioUseCase {
 private final ServicioRepository servicios;private final ConsultaGestionSolicitudPort solicitudes;private final ConsultaGestionPropuestasPort propuestas;private final CuentaEliminadaPort cuentas;
 public ConsultarServicioService(ServicioRepository r,ConsultaGestionSolicitudPort s,ConsultaGestionPropuestasPort p,CuentaEliminadaPort c){servicios=r;solicitudes=s;propuestas=p;cuentas=c;}
 public ServicioResult ejecutar(AccionServicioCommand c){var s=AccesoServicio.participante(servicios,c);
 var sol=solicitudes.porId(s.solicitudId()).orElseThrow(()->AccesoServicio.error(ServicioOperacionException.Tipo.NO_ENCONTRADO,"SOLICITUD_NO_ENCONTRADA","Solicitud no encontrada."));
 var p=propuestas.porId(s.propuestaId()).orElseThrow(()->AccesoServicio.error(ServicioOperacionException.Tipo.NO_ENCONTRADO,"PROPUESTA_NO_ENCONTRADA","Propuesta no encontrada."));
 return new ServicioResult(s.id(),s.solicitudId(),s.propuestaId(),s.clienteId(),s.prestadorId(),s.estado().name(),s.comunicacionHabilitada(),s.fechaContratacion(),cuentas.eliminada(s.clienteId()),cuentas.eliminada(s.prestadorId()),new ServicioResult.SolicitudResumen(sol.id(),sol.descripcion(),sol.categoriaId(),sol.zonaId(),sol.urgencia()==null?null:sol.urgencia().name(),sol.estado().name()),new ServicioResult.PropuestaResumen(p.id(),p.prestadorId(),p.disponibilidad(),p.mensaje(),p.tarifaCop(),p.estado().name(),p.fechaCreacion()));}
}
