package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.exception.AdministracionOperacionException;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.exception.AdministracionOperacionException.Tipo;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.domain.model.AccionModeracion;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.domain.model.TipoAccionModeracion;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.domain.model.TipoObjetivoModeracion;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.domain.repository.AccionModeracionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ResenaModeracionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.VerificacionAdminPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.PerfilModeracionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.PerfilProfesionalModeracionPort;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Decisiones de moderación del administrador (SPEC-12): acciones sobre contenido y resolución de
 * solicitudes de verificación. Moderar contenido no implica bloquear la cuenta; toda mutación
 * genera auditoría inmutable.
 */
public class ModeracionService {
  private final AccionModeracionRepository acciones;
  private final ResenaModeracionPort resenas;
  private final VerificacionAdminPort verificaciones;
  private final com.sebastian.spring.mas.aliado.aliado.common.auditoria.AuditoriaPort
      auditoriaService;
  private final PerfilProfesionalModeracionPort perfilesProfesionales;
  private final PerfilModeracionPort perfiles;

  public ModeracionService(
      AccionModeracionRepository acciones,
      ResenaModeracionPort resenas,
      VerificacionAdminPort verificaciones,
      com.sebastian.spring.mas.aliado.aliado.common.auditoria.AuditoriaPort auditoriaService,
      PerfilProfesionalModeracionPort perfilesProfesionales,
      PerfilModeracionPort perfiles) {
    this.acciones = acciones;
    this.resenas = resenas;
    this.verificaciones = verificaciones;
    this.auditoriaService = auditoriaService;
    this.perfilesProfesionales = perfilesProfesionales;
    this.perfiles = perfiles;
  }

  public void moderar(
      UUID administradorId, String objetivoTipo, UUID objetivoId, String accion, String motivo) {
    if (motivo == null || motivo.isBlank()) {
      throw new AdministracionOperacionException(
          Tipo.DATOS_INVALIDOS, "MOTIVO_REQUERIDO", "El motivo de la moderación es obligatorio.");
    }
    TipoAccionModeracion tipoAccion;
    try {
      tipoAccion = TipoAccionModeracion.valueOf(accion);
    } catch (IllegalArgumentException | NullPointerException ex) {
      throw new AdministracionOperacionException(
          Tipo.DATOS_INVALIDOS, "ACCION_INVALIDA", "Acción de moderación inválida.");
    }
    TipoObjetivoModeracion tipoObjetivo;
    try {
      tipoObjetivo = TipoObjetivoModeracion.valueOf(objetivoTipo);
    } catch (IllegalArgumentException | NullPointerException ex) {
      throw new AdministracionOperacionException(
          Tipo.DATOS_INVALIDOS, "OBJETIVO_INVALIDO", "Objetivo de moderación inválido.");
    }
    // FR-074: moderar contenido puntual sin bloquear la cuenta. El efecto es
    // observable: el contenido deja de mostrarse públicamente. SOLICITAR_CORRECCION
    // oculta igual que OCULTAR_CONTENIDO (así lo exige el test de aceptación de
    // SPEC-12); la diferencia queda en la acción registrada y en la auditoría.
    // Un servicio no tiene contenido ocultable: para él, alto riesgo o bloqueo.
    if (tipoObjetivo == TipoObjetivoModeracion.SERVICIO) {
      throw new AdministracionOperacionException(
          Tipo.DATOS_INVALIDOS,
          "ACCION_NO_APLICABLE",
          "Un servicio no tiene contenido que ocultar; usa alto riesgo o bloqueo de cuenta.");
    }
    acciones.guardar(
        new AccionModeracion(
            UUID.randomUUID(),
            administradorId,
            tipoAccion,
            tipoObjetivo,
            objetivoId,
            motivo.trim(),
            OffsetDateTime.now()));
    switch (tipoObjetivo) {
      case RESENA -> resenas.ocultarResena(objetivoId);
      case PERFIL -> perfilesProfesionales.ocultarPerfil(objetivoId);
      case USUARIO -> perfiles.ocultarPerfil(objetivoId);
      case SERVICIO ->
          throw new AdministracionOperacionException(
              Tipo.DATOS_INVALIDOS,
              "ACCION_NO_APLICABLE",
              "Un servicio no tiene contenido que ocultar.");
    }
    auditoriaService.registrar(
        administradorId, "CONTENT_MODERATED", tipoObjetivo.name(), objetivoId, tipoAccion.name());
  }

  public void resolverVerificacion(
      UUID solicitudId, UUID administradorId, String estado, String justificacion) {
    verificaciones.resolver(solicitudId, estado, administradorId);
    String detalle =
        justificacion == null || justificacion.isBlank()
            ? estado
            : estado + ": " + justificacion.trim();
    auditoriaService.registrar(
        administradorId, "VERIFICATION_RESOLVED", "VERIFICACION", solicitudId, detalle);
  }
}
