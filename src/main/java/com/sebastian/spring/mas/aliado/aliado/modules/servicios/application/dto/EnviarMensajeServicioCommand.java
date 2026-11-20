package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto;

import java.util.UUID;
import java.time.OffsetDateTime;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.MensajeServicio;

public record EnviarMensajeServicioCommand(UUID servicioId, UUID actorId, String contenido) {}
