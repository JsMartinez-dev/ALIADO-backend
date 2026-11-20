package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto;

import java.time.*;
import java.util.*;

public final class AdminResult {
  private AdminResult() {}

  public record CategoriaResponse(UUID id, String nombre, String estado) {}

  public record ZonaResponse(UUID id, String nombre, String coberturaGeografica, String estado) {}

  public record ReporteResponse(
      UUID id,
      String tipo,
      UUID objetivoId,
      String motivo,
      String estado,
      OffsetDateTime fechaCreacion,
      OffsetDateTime detalleRevisadoEn,
      List<EvidenciaResumen> evidencias) {}

  public record EvidenciaResumen(UUID id, String tipoContenido, OffsetDateTime fechaCarga) {}

  public record BloqueoResponse(UUID id, UUID usuarioId, String motivo, boolean activo) {}

  public record AltoRiesgoResponse(UUID id, UUID servicioId, String criterio, boolean activa) {}

  public record AuditoriaResponse(
      UUID id,
      String accion,
      String objetivoTipo,
      UUID objetivoId,
      String detalle,
      OffsetDateTime fecha,
      String traceId) {}

  public record BusquedaResponse(
      List<UsuarioResumen> usuarios, List<SolicitudResumen> solicitudes) {}

  public record UsuarioResumen(UUID id, String email, String rol) {}

  public record SolicitudResumen(UUID id, String descripcion, String estado) {}

  public record EnlaceResponse(UUID id, String nombre, String url, String estado) {}

  public record EvidenciaContenidoResult(byte[] contenido, String tipoContenido) {}
}
