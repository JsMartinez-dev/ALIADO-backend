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
public class HabilitarComunicacionServicioService implements HabilitarComunicacionServicioUseCase {
 public ServicioContratado ejecutar(ServicioContratado s){return new ServicioContratado(s.id(),s.solicitudId(),s.propuestaId(),s.clienteId(),s.prestadorId(),s.estado(),true,s.fechaContratacion(),s.version(),s.historial(),s.mensajes());}
}
