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
final class AccesoServicio {
 private AccesoServicio(){}
 static ServicioContratado participante(ServicioRepository repositorio,AccionServicioCommand c){
  ServicioContratado s=repositorio.porId(c.servicioId()).orElseThrow(()->error(ServicioOperacionException.Tipo.NO_ENCONTRADO,"SERVICIO_NO_ENCONTRADO","Servicio no encontrado."));
  if(!s.esParticipante(c.actorId()))throw error(ServicioOperacionException.Tipo.NO_ENCONTRADO,"SERVICIO_NO_ENCONTRADO","Servicio no encontrado.");return s;
 }
 static ServicioOperacionException error(ServicioOperacionException.Tipo tipo,String codigo,String mensaje){return new ServicioOperacionException(tipo,codigo,mensaje);}
 static void comunicacion(ServicioContratado s){if(!s.comunicacionHabilitada())throw error(ServicioOperacionException.Tipo.CONFLICTO,"COMUNICACION_NO_HABILITADA","La comunicación aún no está habilitada para este servicio.");}
}
