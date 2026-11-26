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
    private final ServicioRepository serviciosRepository;

    public EnviarMensajeServicioService(ServicioRepository serviciosRepository) {
        this.serviciosRepository = serviciosRepository;
    }

    @Override
    public MensajeServicioResult ejecutar(EnviarMensajeServicioCommand command) {
        var servicio = AccesoServicio.participante(serviciosRepository,
                new AccionServicioCommand(command.servicioId(), command.actorId()));
        AccesoServicio.comunicacion(servicio);
        if (command.contenido() == null || command.contenido().isBlank() || command.contenido().length() > 2000) {
            throw AccesoServicio.error(ServicioOperacionException.Tipo.INVALIDO, "MENSAJE_INVALIDO",
                    "El mensaje es obligatorio y admite máximo 2000 caracteres.");
        }

        MensajeServicio mensajeServicio = new MensajeServicio(UUID.randomUUID(), command.servicioId(),
                command.actorId(), command.contenido().trim(), OffsetDateTime.now());
        serviciosRepository.guardar(servicio.conMensaje(mensajeServicio));
        return MensajeServicioResult.desde(mensajeServicio);
    }
}

