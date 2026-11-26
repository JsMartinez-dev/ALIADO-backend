package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.service;
import com.sebastian.spring.mas.aliado.aliado.common.events.Eventos;
import com.sebastian.spring.mas.aliado.aliado.common.events.PublicadorEventos;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.exception.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.port.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.repository.PropuestaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;
import java.time.OffsetDateTime;
import java.util.UUID;

public class CrearPropuestaService implements CrearPropuestaUseCase {
    private final PropuestaRepository propuestasRepository;
    private final ConsultaSolicitudPropuestaPort consultaSolicitudPropuestaPort;
    private final ConsultaCompatibilidadPropuestaPort consultaCompatibilidadPropuestaPort;
    private final PublicadorEventos publicadorEventos;
    private final ValidarDisponibilidadPropuestaUseCase validarDisponibilidadPropuestaUseCase;
    private final ValidarMensajePropuestaUseCase validarMensajePropuestaUseCase;

    public CrearPropuestaService(PropuestaRepository propuestasRepository,
            ConsultaSolicitudPropuestaPort consultaSolicitudPropuestaPort,
            ConsultaCompatibilidadPropuestaPort consultaCompatibilidadPropuestaPort,
            PublicadorEventos publicadorEventos,
            ValidarDisponibilidadPropuestaUseCase validarDisponibilidadPropuestaUseCase,
            ValidarMensajePropuestaUseCase validarMensajePropuestaUseCase) {
        this.propuestasRepository = propuestasRepository;
        this.consultaSolicitudPropuestaPort = consultaSolicitudPropuestaPort;
        this.consultaCompatibilidadPropuestaPort = consultaCompatibilidadPropuestaPort;
        this.publicadorEventos = publicadorEventos;
        this.validarDisponibilidadPropuestaUseCase = validarDisponibilidadPropuestaUseCase;
        this.validarMensajePropuestaUseCase = validarMensajePropuestaUseCase;
    }

    @Override
    public PropuestaResult ejecutar(CrearPropuestaCommand command) {
        String disponibilidadValidada = validarDisponibilidadPropuestaUseCase.ejecutar(command.disponibilidad());
        String mensajeValidado = validarMensajePropuestaUseCase.ejecutar(command.mensaje());
        var solicitudConsultada = consultaSolicitudPropuestaPort.porId(command.solicitudId()).orElseThrow(() ->
                new RecursoPropuestaNoEncontradoException("SOLICITUD_NO_ENCONTRADA", "Solicitud no encontrada."));
        if (solicitudConsultada.estado() != EstadoSolicitud.PUBLICADA) {
            throw new ConflictoPropuestaException("SOLICITUD_NO_ABIERTA", "La solicitud ya no admite nuevas propuestas.");
        }
        if (!consultaCompatibilidadPropuestaPort.esCompatible(command.solicitudId(), command.prestadorId())) {
            throw new ConflictoPropuestaException("SOLICITUD_NO_COMPATIBLE", "La solicitud no es compatible con tu perfil profesional.");
        }
        if (propuestasRepository.activaDePrestadorEnSolicitud(command.solicitudId(), command.prestadorId()).isPresent()) {
            throw new ConflictoPropuestaException("PROPUESTA_DUPLICADA", "Ya tienes una propuesta activa para esta solicitud.");
        }
        OffsetDateTime fechaHoraCreacion = OffsetDateTime.now();
        var propuestaGuardada = propuestasRepository.guardar(new Propuesta(UUID.randomUUID(),
                command.solicitudId(), command.prestadorId(), disponibilidadValidada, mensajeValidado,
                EstadoPropuesta.ACTIVA, fechaHoraCreacion, fechaHoraCreacion, 0));
        publicadorEventos.publicar(new Eventos.PropuestaCreada(propuestaGuardada.id(), command.solicitudId(),
                command.prestadorId(), solicitudConsultada.clienteId(), fechaHoraCreacion));
        return PropuestaResult.desde(propuestaGuardada);
    }
}
