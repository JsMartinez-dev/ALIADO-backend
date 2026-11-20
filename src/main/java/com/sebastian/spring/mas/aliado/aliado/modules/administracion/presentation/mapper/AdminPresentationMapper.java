package com.sebastian.spring.mas.aliado.aliado.modules.administracion.presentation.mapper;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.presentation.dto.AdminDtos;

public final class AdminPresentationMapper {
  private AdminPresentationMapper() {}

  public static AdminDtos.CategoriaResponse aResponse(AdminResult.CategoriaResponse r) {
    return new AdminDtos.CategoriaResponse(r.id(), r.nombre(), r.estado());
  }

  public static AdminDtos.ZonaResponse aResponse(AdminResult.ZonaResponse r) {
    return new AdminDtos.ZonaResponse(r.id(), r.nombre(), r.coberturaGeografica(), r.estado());
  }

  public static AdminDtos.ReporteResponse aResponse(AdminResult.ReporteResponse r) {
    return new AdminDtos.ReporteResponse(
        r.id(),
        r.tipo(),
        r.objetivoId(),
        r.motivo(),
        r.estado(),
        r.fechaCreacion(),
        r.detalleRevisadoEn(),
        r.evidencias().stream()
            .map(v -> new AdminDtos.EvidenciaResumen(v.id(), v.tipoContenido(), v.fechaCarga()))
            .toList());
  }

  public static AdminDtos.BloqueoResponse aResponse(AdminResult.BloqueoResponse r) {
    return new AdminDtos.BloqueoResponse(r.id(), r.usuarioId(), r.motivo(), r.activo());
  }

  public static AdminDtos.AltoRiesgoResponse aResponse(AdminResult.AltoRiesgoResponse r) {
    return new AdminDtos.AltoRiesgoResponse(r.id(), r.servicioId(), r.criterio(), r.activa());
  }

  public static AdminDtos.AuditoriaResponse aResponse(AdminResult.AuditoriaResponse r) {
    return new AdminDtos.AuditoriaResponse(
        r.id(), r.accion(), r.objetivoTipo(), r.objetivoId(), r.detalle(), r.fecha(), r.traceId());
  }

  public static AdminDtos.BusquedaResponse aResponse(AdminResult.BusquedaResponse r) {
    return new AdminDtos.BusquedaResponse(
        r.usuarios().stream()
            .map(v -> new AdminDtos.UsuarioResumen(v.id(), v.email(), v.rol()))
            .toList(),
        r.solicitudes().stream()
            .map(v -> new AdminDtos.SolicitudResumen(v.id(), v.descripcion(), v.estado()))
            .toList());
  }

  public static AdminDtos.EnlaceResponse aResponse(AdminResult.EnlaceResponse r) {
    return new AdminDtos.EnlaceResponse(r.id(), r.nombre(), r.url(), r.estado());
  }
}
