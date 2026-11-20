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
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.dto.PropuestaResult;
public class ListarPropuestasRecibidasService implements ListarPropuestasRecibidasUseCase {
 private final ConsultaGestionSolicitudPort solicitudes;private final ConsultaGestionPropuestasPort propuestas;
 public ListarPropuestasRecibidasService(ConsultaGestionSolicitudPort s,ConsultaGestionPropuestasPort p){solicitudes=s;propuestas=p;}
 public List<PropuestaResult> ejecutar(ListadoPropuestasQuery q){
 var solicitud=solicitudes.porId(q.solicitudId()).filter(s->s.clienteId().equals(q.clienteId())).orElseThrow(()->AccesoServicio.error(ServicioOperacionException.Tipo.NO_ENCONTRADO,"SOLICITUD_NO_ENCONTRADA","Solicitud no encontrada."));
 return propuestas.activasDeSolicitud(solicitud.id()).stream().map(PropuestaResult::desde).toList();}
}
