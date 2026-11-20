package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application;

import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.exception.FormalizacionOperacionException;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.exception.FormalizacionOperacionException.Tipo;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.port.FormalizacionAdminPort;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.domain.model.Enlace;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.domain.model.EstadoEnlace;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.domain.model.Paso;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.domain.model.Progreso;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.domain.model.Ruta;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.domain.repository.EnlaceRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.domain.repository.PasoRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.domain.repository.ProgresoRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.domain.repository.RutaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Formalización y vinculación (SPEC-13, UC069–UC072). El contenido es orientativo: nunca certifica
 * ni declara estatus legal o formal.
 */
public class FormalizacionService implements FormalizacionAdminPort {
  /** Aviso legal visible siempre junto al contenido (SPEC-13). */
  public static final String AVISO_LEGAL =
      "Contenido orientativo e informativo: no certifica ni declara ningún estatus legal o formal.";

  private final RutaRepository rutas;
  private final PasoRepository pasos;
  private final EnlaceRepository enlaces;
  private final ProgresoRepository progresos;

  public FormalizacionService(
      RutaRepository rutas,
      PasoRepository pasos,
      EnlaceRepository enlaces,
      ProgresoRepository progresos) {
    this.rutas = rutas;
    this.pasos = pasos;
    this.enlaces = enlaces;
    this.progresos = progresos;
  }

  public record PasoConEnlaces(Paso paso, List<Enlace> enlaces) {}

  public List<Ruta> rutasActivas() {
    return rutas.activas();
  }

  public List<PasoConEnlaces> pasosDe(UUID rutaId) {
    rutas
        .porId(rutaId)
        .orElseThrow(
            () ->
                new FormalizacionOperacionException(
                    Tipo.NO_ENCONTRADO,
                    "RUTA_NO_ENCONTRADA",
                    "Ruta de formalización no encontrada."));
    return pasos.porRuta(rutaId).stream()
        .map(p -> new PasoConEnlaces(p, enlaces.porPaso(p.id())))
        .toList();
  }

  public Enlace enlace(UUID enlaceId) {
    return enlaces
        .porId(enlaceId)
        .orElseThrow(
            () ->
                new FormalizacionOperacionException(
                    Tipo.NO_ENCONTRADO, "ENLACE_NO_ENCONTRADO", "Enlace no encontrado."));
  }

  public List<Progreso> progreso(UUID prestadorId) {
    List<Progreso> registros =
        rutas.activas().stream()
            .map(r -> progresos.porPrestadorYRuta(prestadorId, r.id()).orElse(null))
            .filter(p -> p != null)
            .toList();
    // Sin progreso previo no hay nada que mostrar: 404, no una lista vacía que
    // parece un progreso en cero (coherente con miVerificacion, SPEC-10).
    if (registros.isEmpty()) {
      throw new FormalizacionOperacionException(
          Tipo.NO_ENCONTRADO,
          "SIN_PROGRESO",
          "Aún no tienes progreso registrado en formalización.");
    }
    return registros;
  }

  /** Gestión admin de enlaces (SPEC-13): deshabilitar o corregir sin tocar la BD a mano. */
  @Override
  public Enlace actualizarEnlace(UUID enlaceId, String nombre, String url, String estado) {
    Enlace actual =
        enlaces
            .porId(enlaceId)
            .orElseThrow(
                () ->
                    new FormalizacionOperacionException(
                        Tipo.NO_ENCONTRADO, "ENLACE_NO_ENCONTRADO", "Enlace no encontrado."));
    if (nombre != null && nombre.isBlank()) {
      throw new FormalizacionOperacionException(
          Tipo.DATOS_INVALIDOS, "NOMBRE_REQUERIDO", "El nombre no puede estar vacío.");
    }
    if (url != null) {
      try {
        java.net.URI destino = java.net.URI.create(url.trim());
        if (!("https".equalsIgnoreCase(destino.getScheme())
                || "http".equalsIgnoreCase(destino.getScheme()))
            || destino.getHost() == null
            || destino.getUserInfo() != null) {
          throw new IllegalArgumentException();
        }
      } catch (IllegalArgumentException ex) {
        throw new FormalizacionOperacionException(
            Tipo.DATOS_INVALIDOS,
            "URL_INVALIDA",
            "El enlace debe tener una URL HTTP o HTTPS válida.");
      }
    }
    EstadoEnlace nuevoEstado = actual.estado();
    if (estado != null) {
      try {
        nuevoEstado = EstadoEnlace.valueOf(estado.trim());
      } catch (IllegalArgumentException ex) {
        throw new FormalizacionOperacionException(
            Tipo.DATOS_INVALIDOS,
            "ESTADO_INVALIDO",
            "Estado inválido: use DISPONIBLE o NO_DISPONIBLE.");
      }
    }
    return enlaces.guardar(
        new Enlace(
            actual.id(),
            actual.pasoId(),
            nombre == null ? actual.nombre() : nombre.trim(),
            url == null ? actual.url() : url.trim(),
            nuevoEstado));
  }

  public Progreso actualizarProgreso(UUID prestadorId, UUID rutaId, int valor) {
    if (valor < 0 || valor > 100) {
      throw new FormalizacionOperacionException(
          Tipo.DATOS_INVALIDOS, "VALOR_INVALIDO", "El valor orientativo debe estar entre 0 y 100.");
    }
    rutas
        .porId(rutaId)
        .orElseThrow(
            () ->
                new FormalizacionOperacionException(
                    Tipo.NO_ENCONTRADO,
                    "RUTA_NO_ENCONTRADA",
                    "Ruta de formalización no encontrada."));
    Progreso existente = progresos.porPrestadorYRuta(prestadorId, rutaId).orElse(null);
    Progreso progreso =
        new Progreso(
            existente == null ? UUID.randomUUID() : existente.id(),
            prestadorId,
            rutaId,
            valor,
            OffsetDateTime.now());
    return progresos.guardar(progreso);
  }
}
