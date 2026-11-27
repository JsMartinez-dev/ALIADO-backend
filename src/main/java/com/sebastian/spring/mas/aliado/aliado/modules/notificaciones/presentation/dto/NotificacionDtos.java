package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Contratos HTTP de la bandeja de notificaciones y dispositivos push (SPEC-09, F09-PUSH-01). */
public final class NotificacionDtos {
    private NotificacionDtos() {}

    public record NotificacionResponse(UUID id, String tipo, UUID servicioId, UUID solicitudId, UUID propuestaId, String detalle,
                                       String estadoEntrega, OffsetDateTime fechaGeneracion) {}

    /**
     * Aviso de una página de la bandeja (F09-BANDEJA-04): conserva los campos del contrato
     * vigente y agrega {@code leidaEn}, que viaja como {@code null} mientras esté pendiente y
     * en ISO 8601 cuando la cuenta ya lo leyó. Es un campo distinto de {@code estadoEntrega}:
     * la lectura no depende del resultado del envío push.
     */
    public record AvisoBandejaResponse(UUID id, String tipo, UUID servicioId, UUID solicitudId, UUID propuestaId, String detalle,
                                       String estadoEntrega, OffsetDateTime fechaGeneracion,
                                       OffsetDateTime leidaEn) {}

    /** GET /api/v1/notificaciones/bandeja: página propia y cursor de la siguiente, o null. */
    public record BandejaResponse(List<AvisoBandejaResponse> items, String cursorSiguiente) {}

    /** GET /api/v1/notificaciones/resumen: indicador de la campana sin cargar la bandeja. */
    public record ResumenNoLeidasResponse(boolean hayNoLeidas) {}

    /** PUT /api/v1/notificaciones/dispositivos/{instalacionId}. Nunca devuelve secretos. */
    public record DispositivoPushRequest(@NotBlank String plataforma, @NotNull Boolean permisoConcedido,
                                         @Size(max = 4096) String token, String credencialRevocacion) {}

    /**
     * GET /api/v1/notificaciones/{notificacionId}/oportunidad (F09-PUSH-10). Solo expone el
     * estado mínimo permitido al destinatario; nunca descripción, ubicación ni contacto.
     */
    public record OportunidadDesdeAvisoResponse(UUID solicitudId, String estado, boolean puedeCotizar) {}
}
