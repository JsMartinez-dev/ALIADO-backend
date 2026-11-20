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
public class EnviarMensajeServicioService implements EnviarMensajeServicioUseCase {
 private final ServicioRepository servicios;public EnviarMensajeServicioService(ServicioRepository r){servicios=r;}
 public MensajeServicioResult ejecutar(EnviarMensajeServicioCommand c){var s=AccesoServicio.participante(servicios,new AccionServicioCommand(c.servicioId(),c.actorId()));AccesoServicio.comunicacion(s);
 if(c.contenido()==null||c.contenido().isBlank()||c.contenido().length()>2000)throw AccesoServicio.error(ServicioOperacionException.Tipo.INVALIDO,"MENSAJE_INVALIDO","El mensaje es obligatorio y admite máximo 2000 caracteres.");
 var m=new MensajeServicio(UUID.randomUUID(),c.servicioId(),c.actorId(),c.contenido().trim(),OffsetDateTime.now());servicios.guardar(s.conMensaje(m));return MensajeServicioResult.desde(m);}
}
