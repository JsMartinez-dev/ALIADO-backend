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
public class MarcarServicioEnEjecucionService implements MarcarServicioEnEjecucionUseCase {
 private final ServicioRepository servicios; private final PublicadorEventos eventos;
 public MarcarServicioEnEjecucionService(ServicioRepository r,PublicadorEventos e){servicios=r;eventos=e;}
 public ServicioResult ejecutar(AccionServicioCommand c){
  return TransicionServicio.ejecutar(servicios,eventos,c,EstadoServicio.EN_EJECUCION,"PRESTADOR","Solo el prestador del servicio puede iniciarlo.");}
}
