package com.sebastian.spring.mas.aliado.aliado.modules.servicios.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Contratos HTTP de contratación y servicio (SPEC-08). */
public final class ServicioDtos {
    private ServicioDtos() {}

    public record ServicioResponse(UUID id, UUID solicitudId, UUID propuestaId, UUID clienteId, UUID prestadorId,
                                   String estado, boolean comunicacionHabilitada, OffsetDateTime fechaContratacion,
                                   boolean clienteEliminado, boolean prestadorEliminado,
                                   SolicitudResumen solicitud, PropuestaResumen propuesta) {}

    public record SolicitudResumen(UUID id, String descripcion, UUID categoriaId, UUID zonaId, String urgencia,
                                   String estado) {}

    public record PropuestaResumen(UUID id, UUID prestadorId, String disponibilidad, String mensaje, Integer tarifaCop,
                                   String estado, OffsetDateTime fechaCreacion) {}

    public record ServicioResumenResponse(UUID id, UUID solicitudId, UUID propuestaId, String estado,
                                          OffsetDateTime fechaContratacion) {}

    public record ServiciosPropiosPage(List<ServicioResumenResponse> items, int pagina, int tamano,
                                       long totalElementos, int totalPaginas) {}

    public record AceptarResponse(UUID servicioId, String estado) {}

    public record HistorialResponse(String estadoAnterior, String estadoNuevo,
                                    UUID actorId, boolean actorEliminado, OffsetDateTime fecha) {}

    public record MensajeRequest(@NotBlank String contenido) {}

    @Schema(name = "ServicioMensajeResponse")
    public record MensajeResponse(UUID id, UUID remitenteId, String contenido, boolean remitenteEliminado, OffsetDateTime fechaEnvio) {}

    public record IdEstadoResponse(UUID id, String estado) {}
}
