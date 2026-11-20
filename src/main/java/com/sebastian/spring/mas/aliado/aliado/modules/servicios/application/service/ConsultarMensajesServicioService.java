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
public class ConsultarMensajesServicioService implements ConsultarMensajesServicioUseCase {
 private final ServicioRepository servicios;public ConsultarMensajesServicioService(ServicioRepository r){servicios=r;}
 public List<MensajeServicioResult> ejecutar(AccionServicioCommand c){var s=AccesoServicio.participante(servicios,c);AccesoServicio.comunicacion(s);return s.mensajes().stream().map(MensajeServicioResult::desde).toList();}
}
