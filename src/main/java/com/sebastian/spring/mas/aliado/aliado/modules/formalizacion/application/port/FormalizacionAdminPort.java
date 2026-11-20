package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.port;

import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.domain.model.Enlace;
import java.util.UUID;

/** Gestión administrativa de enlaces de formalización (SPEC-13). */
public interface FormalizacionAdminPort {
  /** Actualiza nombre, URL y/o estado de un enlace (campos nulos = sin cambio). */
  Enlace actualizarEnlace(UUID enlaceId, String nombre, String url, String estado);
}
