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
final class TransicionServicio {
 private TransicionServicio(){}
 static ServicioResult ejecutar(ServicioRepository r,PublicadorEventos e,AccionServicioCommand c,EstadoServicio nuevo,String rol,String mensaje){
 var s=AccesoServicio.participante(r,c);
 if(rol.equals("PRESTADOR")&&!s.prestadorId().equals(c.actorId())||rol.equals("CLIENTE")&&!s.clienteId().equals(c.actorId()))throw AccesoServicio.error(ServicioOperacionException.Tipo.NO_AUTORIZADO,"NO_AUTORIZADO",mensaje);
 var ahora=OffsetDateTime.now();var actualizado=r.guardar(s.transitarA(nuevo,c.actorId(),ahora));
 e.publicar(new Eventos.ServicioEstadoCambiado(actualizado.id(),actualizado.clienteId(),actualizado.prestadorId(),s.estado().name(),nuevo.name(),c.actorId(),ahora,nuevo.ordinal()+1));return ServicioResult.desde(actualizado);}
}
