package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port;

import java.util.Optional;
import java.util.UUID;

public interface ReferenciaPropuestaAvisoPort {
    Optional<UUID> solicitudPropia(UUID propuestaId, UUID destinatarioId);
}
