package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Contratos HTTP de la bandeja de notificaciones y dispositivos push (SPEC-09, F09-PUSH-01). */
public final class NotificacionDtos {
    private NotificacionDtos() {}

    public record NotificacionResponse(UUID id, String tipo, UUID servicioId, UUID solicitudId, String detalle,
                                       String estadoEntrega, OffsetDateTime fechaGeneracion) {}

    /** PUT /api/v1/notificaciones/dispositivos/{instalacionId}. Nunca devuelve secretos. */
    public record DispositivoPushRequest(@NotBlank String plataforma, @NotNull Boolean permisoConcedido,
                                         @Size(max = 4096) String token, String credencialRevocacion) {}

    /**
     * GET /api/v1/notificaciones/{notificacionId}/oportunidad (F09-PUSH-10). Solo expone el
     * estado mínimo permitido al destinatario; nunca descripción, ubicación ni contacto.
     */
    public record OportunidadDesdeAvisoResponse(UUID solicitudId, String estado, boolean puedeCotizar) {}
}
