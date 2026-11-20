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
public class RegistrarServicioContratadoService implements RegistrarServicioContratadoUseCase {
 private final ServicioRepository servicios;private final HabilitarComunicacionServicioUseCase habilitar;
 public RegistrarServicioContratadoService(ServicioRepository r,HabilitarComunicacionServicioUseCase h){servicios=r;habilitar=h;}
 public ServicioContratado ejecutar(RegistrarServicioCommand c){UUID id=UUID.randomUUID();var s=new ServicioContratado(id,c.solicitudId(),c.propuestaId(),c.clienteId(),c.prestadorId(),EstadoServicio.CONTRATADO,false,c.fecha(),0,List.of(new HistorialEstadoServicio(UUID.randomUUID(),id,null,EstadoServicio.CONTRATADO,c.clienteId(),c.fecha())),List.of());return servicios.guardar(habilitar.ejecutar(s));}
}
