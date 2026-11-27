package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application;

import com.sebastian.spring.mas.aliado.aliado.common.auditoria.AuditoriaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.domain.model.Auditoria;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.domain.repository.AuditoriaRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Escritura de auditoría append-only (RNF-005). El detalle nunca lleva datos personales. */
@Service
public class AuditoriaService implements AuditoriaPort {

  private final AuditoriaRepository auditoriaRepository;

  public AuditoriaService(AuditoriaRepository auditoriaRepository) {
    this.auditoriaRepository = auditoriaRepository;
  }

  @Override
  @Transactional
  public void registrar(
      UUID administradorId, String accion, String objetivoTipo, UUID objetivoId, String detalle) {
    String detalleSeguro =
        detalle == null
            ? null
            : detalle.replaceAll("(?i)(password|token|correo|email|direccion)[^,;]*", "[omitido]");
    auditoriaRepository.guardar(
        new Auditoria(
            UUID.randomUUID(),
            administradorId,
            accion,
            objetivoTipo,
            objetivoId,
            detalleSeguro,
            OffsetDateTime.now(),
            UUID.randomUUID().toString()));
  }
}
