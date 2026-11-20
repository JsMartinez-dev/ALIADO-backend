package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.exception.AdministracionOperacionException;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.exception.AdministracionOperacionException.Tipo;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.domain.model.BloqueoPreventivo;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.domain.repository.BloqueoRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.CuentaBloqueoPort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.UsuarioConsultaPort;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Bloqueo preventivo de cuentas (SPEC-12, UC071–UC072). La moderación de contenido es independiente
 * del bloqueo de cuenta.
 */
public class BloqueoCuentaService {
  private final com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.port
          .ConsultaEliminacionCuentaPort
      eliminaciones;
  private final BloqueoRepository bloqueos;
  private final UsuarioConsultaPort usuarios;
  private final CuentaBloqueoPort cuentas;
  private final com.sebastian.spring.mas.aliado.aliado.common.auditoria.AuditoriaPort
      auditoriaService;

  public BloqueoCuentaService(
      BloqueoRepository bloqueos,
      com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.port
              .ConsultaEliminacionCuentaPort
          eliminaciones,
      UsuarioConsultaPort usuarios,
      CuentaBloqueoPort cuentas,
      com.sebastian.spring.mas.aliado.aliado.common.auditoria.AuditoriaPort auditoriaService) {
    this.bloqueos = bloqueos;
    this.eliminaciones = eliminaciones;
    this.usuarios = usuarios;
    this.cuentas = cuentas;
    this.auditoriaService = auditoriaService;
  }

  public BloqueoPreventivo bloquearUsuario(UUID usuarioId, UUID administradorId, String motivo) {
    if (!usuarios.existe(usuarioId)) {
      throw new AdministracionOperacionException(
          Tipo.NO_ENCONTRADO, "USUARIO_NO_ENCONTRADO", "Usuario no encontrado.");
    }
    if (motivo == null || motivo.isBlank()) {
      throw new AdministracionOperacionException(
          Tipo.DATOS_INVALIDOS, "MOTIVO_REQUERIDO", "El motivo del bloqueo es obligatorio.");
    }
    if (bloqueos.activoDeUsuario(usuarioId).isPresent()) {
      throw new AdministracionOperacionException(
          Tipo.CONFLICTO, "BLOQUEO_ACTIVO", "El usuario ya tiene un bloqueo activo.");
    }
    BloqueoPreventivo bloqueo =
        bloqueos.guardar(
            new BloqueoPreventivo(
                UUID.randomUUID(),
                usuarioId,
                administradorId,
                motivo.trim(),
                OffsetDateTime.now(),
                null,
                true));
    cuentas.bloquearCuenta(usuarioId);
    auditoriaService.registrar(
        administradorId, "ACCOUNT_BLOCKED", "USUARIO", usuarioId, motivo.trim());
    return bloqueo;
  }

  public void levantarBloqueo(UUID usuarioId, UUID bloqueoId, UUID administradorId) {
    BloqueoPreventivo bloqueo =
        bloqueos
            .porId(bloqueoId)
            .filter(b -> b.usuarioId().equals(usuarioId) && b.activo())
            .orElseThrow(
                () ->
                    new AdministracionOperacionException(
                        Tipo.NO_ENCONTRADO,
                        "BLOQUEO_NO_ENCONTRADO",
                        "Bloqueo activo no encontrado."));
    bloqueos.guardar(
        new BloqueoPreventivo(
            bloqueo.id(),
            usuarioId,
            bloqueo.administradorId(),
            bloqueo.motivo(),
            bloqueo.fechaInicio(),
            OffsetDateTime.now(),
            false));
    if (!eliminaciones.tieneEliminacionConfirmada(usuarioId)) {
      cuentas.desbloquearCuenta(usuarioId);
    }
    auditoriaService.registrar(administradorId, "ACCOUNT_UNBLOCKED", "USUARIO", usuarioId, null);
  }
}
