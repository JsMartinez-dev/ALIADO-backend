package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Contratos HTTP de solicitudes (SPEC-04). */
public final class SolicitudDtos {
    private SolicitudDtos() {}

    @Schema(name = "CrearSolicitudRequest",
            description = "Datos para crear una solicitud como borrador o publicarla.")
    public record CrearRequest(
            @Schema(description = "Identificador de la categoría del servicio.", example = "7f4e3b2a-1c0d-4e5f-9a8b-6c7d8e9f0a1b")
            UUID categoriaId,
            @Schema(description = "Descripción del trabajo o servicio requerido.",
                    example = "Reparación de una fuga en la cocina.")
            String descripcion,
            @Schema(description = "Identificador de la zona donde se requiere el servicio.",
                    example = "8f4e3b2a-1c0d-4e5f-9a8b-6c7d8e9f0a1b")
            UUID zonaId,
            @Schema(description = "Nivel de urgencia.", allowableValues = {"BAJA", "MEDIA", "ALTA", "URGENTE"},
                    example = "MEDIA")
            String urgencia,
            @Schema(description = "Estado inicial deseado. Si se omite, la solicitud se publica.",
                    allowableValues = {"BORRADOR", "PUBLICADA"}, example = "BORRADOR")
            String estadoDeseado) {}

    @Schema(name = "EditarSolicitudRequest", description = "Campos que se desean actualizar en una solicitud propia.")
    public record EditarRequest(
            @Schema(description = "Nuevo identificador de categoría.", example = "7f4e3b2a-1c0d-4e5f-9a8b-6c7d8e9f0a1b")
            UUID categoriaId,
            @Schema(description = "Nueva descripción del trabajo requerido.",
                    example = "Reparación de una fuga en el baño.")
            String descripcion,
            @Schema(description = "Nuevo identificador de zona.",
                    example = "8f4e3b2a-1c0d-4e5f-9a8b-6c7d8e9f0a1b")
            UUID zonaId,
            @Schema(description = "Nuevo nivel de urgencia.",
                    allowableValues = {"BAJA", "MEDIA", "ALTA", "URGENTE"}, example = "ALTA")
            String urgencia) {}

    @Schema(name = "SolicitudResponse", description = "Datos de una solicitud de servicio.")
    public record SolicitudResponse(
            @Schema(description = "Identificador único de la solicitud.")
            UUID id,
            @Schema(description = "Identificador del cliente propietario de la solicitud.")
            UUID clienteId,
            @Schema(description = "Identificador de la categoría de servicio.")
            UUID categoriaId,
            @Schema(description = "Identificador de la zona de servicio.")
            UUID zonaId,
            @Schema(description = "Descripción del trabajo o servicio requerido.")
            String descripcion,
            @Schema(description = "Nivel de urgencia seleccionado.")
            String urgencia,
            @Schema(description = "Estado actual de la solicitud.")
            String estado,
            @Schema(description = "Fecha y hora de creación, con zona horaria.")
            OffsetDateTime fechaCreacion,
            @Schema(description = "Fecha y hora de la última actualización, con zona horaria.")
            OffsetDateTime fechaActualizacion,
            @Schema(description = "Claves de almacenamiento de las fotos adjuntas.")
            List<String> fotos) {}

    @Schema(name = "FotoSolicitudResponse", description = "Referencia a una foto adjunta a una solicitud.")
    public record FotoResponse(
            @Schema(description = "Identificador único de la foto.")
            UUID id,
            @Schema(description = "Clave de almacenamiento asignada al archivo.")
            String archivoKey,
            @Schema(description = "Posición de la foto dentro de la solicitud.", example = "1")
            int orden) {}

    @Schema(name = "SolicitudEstadoResponse", description = "Identificador y estado actual de una solicitud.")
    public record IdEstadoResponse(
            @Schema(description = "Identificador único de la solicitud.")
            UUID id,
            @Schema(description = "Estado actual de la solicitud.")
            String estado) {}
}
