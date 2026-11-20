package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.UsuarioConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.PerfilBusquedaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.SolicitudConsultaPort;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Búsqueda administrativa (SPEC-12, UC075/FR-075): cuentas y perfiles por nombre, correo o
 * identificador, y solicitudes por descripción o identificador. Los perfiles hallados por nombre se
 * resuelven a su cuenta (correo y rol) para devolver un único listado de usuarios sin duplicados.
 */
public class BusquedaAdminService {
  private final UsuarioConsultaPort usuarios;
  private final PerfilBusquedaPort perfiles;
  private final SolicitudConsultaPort solicitudes;

  public BusquedaAdminService(
      UsuarioConsultaPort usuarios,
      PerfilBusquedaPort perfiles,
      SolicitudConsultaPort solicitudes) {
    this.usuarios = usuarios;
    this.perfiles = perfiles;
    this.solicitudes = solicitudes;
  }

  public record UsuarioResumen(UUID id, String email, String rol) {}

  public record SolicitudResumen(UUID id, String descripcion, String estado) {}

  public record ResultadoBusqueda(
      List<UsuarioResumen> usuarios, List<SolicitudResumen> solicitudes) {}

  public ResultadoBusqueda buscar(String texto) {
    if (texto == null || texto.isBlank()) {
      throw new com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.exception
          .AdministracionOperacionException(
          com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.exception
              .AdministracionOperacionException.Tipo.DATOS_INVALIDOS,
          "BUSQUEDA_REQUERIDA",
          "El parámetro q es obligatorio para buscar.");
    }
    texto = texto.trim();
    Map<UUID, UsuarioResumen> hallados = new LinkedHashMap<>();
    usuarios
        .buscarPorTexto(texto)
        .forEach(u -> hallados.put(u.id(), new UsuarioResumen(u.id(), u.email(), u.rol().name())));
    perfiles
        .buscarPorNombre(texto)
        .forEach(
            p ->
                usuarios
                    .porId(p.usuarioId())
                    .ifPresent(
                        u ->
                            hallados.putIfAbsent(
                                u.id(), new UsuarioResumen(u.id(), u.email(), u.rol().name()))));
    List<SolicitudResumen> solicitudesHalladas =
        solicitudes.buscarPorTexto(texto).stream()
            .map(s -> new SolicitudResumen(s.id(), s.descripcion(), s.estado().name()))
            .toList();
    return new ResultadoBusqueda(new ArrayList<>(hallados.values()), solicitudesHalladas);
  }
}
