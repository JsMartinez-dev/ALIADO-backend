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
    private TransicionServicio() {}

    static ServicioResult ejecutar(ServicioRepository serviciosRepository, PublicadorEventos publicadorEventos,
            AccionServicioCommand accionServicioCommand, EstadoServicio nuevoEstado,
            String rolRequerido, String mensajeNoAutorizado) {

        ServicioContratado servicioActual = AccesoServicio.participante(serviciosRepository, accionServicioCommand);

        boolean esPrestadorNoAutorizado = rolRequerido.equals("PRESTADOR") && !servicioActual.prestadorId().equals(accionServicioCommand.actorId());

        boolean esClienteNoAutorizado = rolRequerido.equals("CLIENTE") && !servicioActual.clienteId().equals(accionServicioCommand.actorId());

        if (esPrestadorNoAutorizado || esClienteNoAutorizado) {
            throw AccesoServicio.error(ServicioOperacionException.Tipo.NO_AUTORIZADO,
                    "NO_AUTORIZADO", mensajeNoAutorizado);
        }

        OffsetDateTime fechaHoraTransicion = OffsetDateTime.now();
        ServicioContratado servicioActualizado = serviciosRepository.guardar(
                servicioActual.transitarA(nuevoEstado, accionServicioCommand.actorId(), fechaHoraTransicion));

        publicadorEventos.publicar(new Eventos.ServicioEstadoCambiado(
                servicioActualizado.id(), servicioActualizado.clienteId(), servicioActualizado.prestadorId(),
                servicioActual.estado().name(), nuevoEstado.name(), accionServicioCommand.actorId(),
                fechaHoraTransicion, nuevoEstado.ordinal() + 1));

        return ServicioResult.desde(servicioActualizado);
    }
}
