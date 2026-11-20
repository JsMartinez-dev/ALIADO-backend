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
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model.*;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;
public class AceptarPropuestaService implements AceptarPropuestaUseCase {
 private final ServicioRepository servicios;private final ConsultaGestionPropuestasPort propuestas;private final ConsultaGestionSolicitudPort solicitudes;private final PublicadorEventos eventos;private final RegistrarServicioContratadoUseCase registrar;private final CerrarPropuestasNoSeleccionadasUseCase cerrar;
 public AceptarPropuestaService(ServicioRepository r,ConsultaGestionPropuestasPort p,ConsultaGestionSolicitudPort s,PublicadorEventos e,RegistrarServicioContratadoUseCase registro,CerrarPropuestasNoSeleccionadasUseCase cierre){servicios=r;propuestas=p;solicitudes=s;eventos=e;registrar=registro;cerrar=cierre;}
 public ServicioResult ejecutar(AceptarPropuestaCommand c){
        Propuesta propuesta = propuestas.porId(c.propuestaId())
                .orElseThrow(() -> AccesoServicio.error(ServicioOperacionException.Tipo.NO_ENCONTRADO,"PROPUESTA_NO_ENCONTRADA",
                        "Propuesta no encontrada."));
        var solicitud = solicitudes.porId(propuesta.solicitudId())
                .orElseThrow(() -> AccesoServicio.error(ServicioOperacionException.Tipo.NO_ENCONTRADO,"SOLICITUD_NO_ENCONTRADA",
                        "Solicitud no encontrada."));
        if (!solicitud.clienteId().equals(c.clienteId())) {
            throw AccesoServicio.error(ServicioOperacionException.Tipo.NO_AUTORIZADO,"NO_AUTORIZADO",
                    "Solo el cliente dueño de la solicitud puede aceptar propuestas.");
        }
        if (propuesta.estado() != EstadoPropuesta.ACTIVA) {
            throw AccesoServicio.error(ServicioOperacionException.Tipo.CONFLICTO,"PROPUESTA_NO_ACTIVA", "La propuesta ya no está activa.");
        }
        if (solicitud.estado() != EstadoSolicitud.PUBLICADA) {
            throw AccesoServicio.error(ServicioOperacionException.Tipo.CONFLICTO,"SOLICITUD_NO_ABIERTA", "La solicitud ya no está abierta.");
        }
        if (servicios.porSolicitudId(solicitud.id()).isPresent()) {
            throw AccesoServicio.error(ServicioOperacionException.Tipo.CONFLICTO,"SERVICIO_YA_CONTRATADO",
                    "La solicitud ya tiene un servicio contratado.");
        }
        OffsetDateTime ahora = OffsetDateTime.now();
        ServicioContratado guardado = registrar.ejecutar(new RegistrarServicioCommand(solicitud.id(),propuesta.id(),c.clienteId(),propuesta.prestadorId(),ahora));
        propuestas.marcarAceptada(propuesta.id());
        cerrar.ejecutar(new CerrarPropuestasCommand(solicitud.id(), propuesta.id()));
        solicitudes.marcarContratada(solicitud.id());
        eventos.publicar(new Eventos.PropuestaAceptada(guardado.id(), propuesta.id(), solicitud.id(),
                c.clienteId(), propuesta.prestadorId(), ahora));
        return ServicioResult.desde(guardado);
 }
}
