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
    private final ServicioRepository serviciosRepository;
    private final ConsultaGestionPropuestasPort consultaGestionPropuestasPort;
    private final ConsultaGestionSolicitudPort consultaGestionSolicitudPort;
    private final PublicadorEventos publicadorEventos;
    private final RegistrarServicioContratadoUseCase registrarServicioContratadoUseCase;
    private final CerrarPropuestasNoSeleccionadasUseCase cerrarPropuestasNoSeleccionadasUseCase;

    public AceptarPropuestaService(ServicioRepository serviciosRepository,
            ConsultaGestionPropuestasPort consultaGestionPropuestasPort,
            ConsultaGestionSolicitudPort consultaGestionSolicitudPort,
            PublicadorEventos publicadorEventos,
            RegistrarServicioContratadoUseCase registrarServicioContratadoUseCase,
            CerrarPropuestasNoSeleccionadasUseCase cerrarPropuestasNoSeleccionadasUseCase) {
        this.serviciosRepository = serviciosRepository;
        this.consultaGestionPropuestasPort = consultaGestionPropuestasPort;
        this.consultaGestionSolicitudPort = consultaGestionSolicitudPort;
        this.publicadorEventos = publicadorEventos;
        this.registrarServicioContratadoUseCase = registrarServicioContratadoUseCase;
        this.cerrarPropuestasNoSeleccionadasUseCase = cerrarPropuestasNoSeleccionadasUseCase;
    }

    @Override
    public ServicioResult ejecutar(AceptarPropuestaCommand command) {
        Propuesta propuestaSeleccionada = consultaGestionPropuestasPort.porId(command.propuestaId())
                .orElseThrow(() -> AccesoServicio.error(ServicioOperacionException.Tipo.NO_ENCONTRADO,"PROPUESTA_NO_ENCONTRADA",
                        "Propuesta no encontrada."));
        var solicitudAsociada = consultaGestionSolicitudPort.porId(propuestaSeleccionada.solicitudId())
                .orElseThrow(() -> AccesoServicio.error(ServicioOperacionException.Tipo.NO_ENCONTRADO,"SOLICITUD_NO_ENCONTRADA",
                        "Solicitud no encontrada."));
        if (!solicitudAsociada.clienteId().equals(command.clienteId())) {
            throw AccesoServicio.error(ServicioOperacionException.Tipo.NO_AUTORIZADO,"NO_AUTORIZADO",
                    "Solo el cliente dueño de la solicitud puede aceptar propuestas.");
        }
        if (propuestaSeleccionada.estado() != EstadoPropuesta.ACTIVA) {
            throw AccesoServicio.error(ServicioOperacionException.Tipo.CONFLICTO,"PROPUESTA_NO_ACTIVA", "La propuesta ya no está activa.");
        }
        if (solicitudAsociada.estado() != EstadoSolicitud.PUBLICADA) {
            throw AccesoServicio.error(ServicioOperacionException.Tipo.CONFLICTO,"SOLICITUD_NO_ABIERTA", "La solicitud ya no está abierta.");
        }
        if (serviciosRepository.porSolicitudId(solicitudAsociada.id()).isPresent()) {
            throw AccesoServicio.error(ServicioOperacionException.Tipo.CONFLICTO,"SERVICIO_YA_CONTRATADO",
                    "La solicitud ya tiene un servicio contratado.");
        }
        OffsetDateTime fechaHoraAceptacion = OffsetDateTime.now();
        ServicioContratado servicioContratado = registrarServicioContratadoUseCase.ejecutar(
                new RegistrarServicioCommand(solicitudAsociada.id(), propuestaSeleccionada.id(),
                        command.clienteId(), propuestaSeleccionada.prestadorId(), fechaHoraAceptacion));
        consultaGestionPropuestasPort.marcarAceptada(propuestaSeleccionada.id());
        cerrarPropuestasNoSeleccionadasUseCase.ejecutar(
                new CerrarPropuestasCommand(solicitudAsociada.id(), propuestaSeleccionada.id()));
        consultaGestionSolicitudPort.marcarContratada(solicitudAsociada.id());
        publicadorEventos.publicar(new Eventos.PropuestaAceptada(servicioContratado.id(),
                propuestaSeleccionada.id(), solicitudAsociada.id(), command.clienteId(),
                propuestaSeleccionada.prestadorId(), fechaHoraAceptacion));
        return ServicioResult.desde(servicioContratado);
    }
}
