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
public class ConsultarHistorialServicioService implements ConsultarHistorialServicioUseCase {
 private final ServicioRepository servicios;public ConsultarHistorialServicioService(ServicioRepository r){servicios=r;}
 public List<HistorialServicioResult> ejecutar(AccionServicioCommand c){return AccesoServicio.participante(servicios,c).historial().stream().map(HistorialServicioResult::desde).toList();}
}
