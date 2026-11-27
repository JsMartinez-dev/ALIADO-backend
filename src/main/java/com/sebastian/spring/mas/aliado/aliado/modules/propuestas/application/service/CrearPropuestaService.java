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
/** UC039: orquesta la creación sin detalles HTTP ni persistencia concreta. */
public class CrearPropuestaService implements CrearPropuestaUseCase {
    private final PropuestaRepository propuestas;
    private final ConsultaSolicitudPropuestaPort solicitudes;
    private final ConsultaCompatibilidadPropuestaPort compatibilidad;
    private final PublicadorEventos eventos;
    private final ValidarDisponibilidadPropuestaUseCase disponibilidad;
    private final ValidarMensajePropuestaUseCase mensaje;
    public CrearPropuestaService(PropuestaRepository propuestas, ConsultaSolicitudPropuestaPort solicitudes,
            ConsultaCompatibilidadPropuestaPort compatibilidad, PublicadorEventos eventos,
            ValidarDisponibilidadPropuestaUseCase disponibilidad, ValidarMensajePropuestaUseCase mensaje) {
        this.propuestas = propuestas; this.solicitudes = solicitudes; this.compatibilidad = compatibilidad;
        this.eventos = eventos; this.disponibilidad = disponibilidad; this.mensaje = mensaje;
    }
    @Override public PropuestaResult ejecutar(CrearPropuestaCommand command) {
        String disponibilidadValidada = disponibilidad.ejecutar(command.disponibilidad());
        String mensajeValidado = mensaje.ejecutar(command.mensaje());
        Integer tarifaValidada = ValidarTarifaPropuesta.validar(command.tarifaCop(), true);
        var solicitud = solicitudes.porId(command.solicitudId()).orElseThrow(() ->
                new RecursoPropuestaNoEncontradoException("SOLICITUD_NO_ENCONTRADA", "Solicitud no encontrada."));
        if (solicitud.estado() != EstadoSolicitud.PUBLICADA) {
            throw new ConflictoPropuestaException("SOLICITUD_NO_ABIERTA", "La solicitud ya no admite nuevas propuestas.");
        }
        if (!compatibilidad.esCompatible(command.solicitudId(), command.prestadorId())) {
            throw new ConflictoPropuestaException("SOLICITUD_NO_COMPATIBLE", "La solicitud no es compatible con tu perfil profesional.");
        }
        if (propuestas.activaDePrestadorEnSolicitud(command.solicitudId(), command.prestadorId()).isPresent()) {
            throw new ConflictoPropuestaException("PROPUESTA_DUPLICADA", "Ya tienes una propuesta activa para esta solicitud.");
        }
        OffsetDateTime ahora = OffsetDateTime.now();
        var guardada = propuestas.guardar(new Propuesta(UUID.randomUUID(), command.solicitudId(), command.prestadorId(),
                disponibilidadValidada, mensajeValidado, tarifaValidada, EstadoPropuesta.ACTIVA, ahora, ahora, 0));
        eventos.publicar(new Eventos.PropuestaCreada(guardada.id(), command.solicitudId(), command.prestadorId(), solicitud.clienteId(), ahora));
        return PropuestaResult.desde(guardada);
    }
}
