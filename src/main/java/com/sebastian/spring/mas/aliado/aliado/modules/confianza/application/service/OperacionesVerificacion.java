package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoStorageService;
import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoValidacion;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.model.DocumentoVerificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.model.EstadoVerificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.model.SolicitudVerificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.DocumentoVerificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.SolicitudVerificacionRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class OperacionesVerificacion {
    private final SolicitudVerificacionRepository solicitudVerificacionRepository;
    private final DocumentoVerificacionRepository documentoVerificacionRepository;
    private final ArchivoStorageService archivoStorageService;

    public OperacionesVerificacion(SolicitudVerificacionRepository solicitudVerificacionRepository,
                                   DocumentoVerificacionRepository documentoVerificacionRepository, ArchivoStorageService archivoStorageService) {
        this.solicitudVerificacionRepository = solicitudVerificacionRepository;
        this.documentoVerificacionRepository = documentoVerificacionRepository;
        this.archivoStorageService = archivoStorageService;
    }

    public record ArchivoSubido(String nombreOriginal, String tipoContenido, byte[] contenido) {}
    public SolicitudVerificacion solicitarVerificacion(UUID prestadorId, List<ArchivoSubido> archivos) {
        if (solicitudVerificacionRepository.pendienteDePrestador(prestadorId).isPresent()) {
            throw new BusinessException.Conflict("VERIFICACION_PENDIENTE",
                    "Ya tienes una solicitud de verificación pendiente.");
        }

        if (solicitudVerificacionRepository.contarAprobadasDePrestador(prestadorId) > 0) {
            throw new BusinessException.Conflict("NIVEL_MAXIMO_ALCANZADO",
                    "Ya alcanzaste el nivel máximo de verificación (BÁSICO).");
        }
        if (archivos == null || archivos.isEmpty()) {
            throw new BusinessException.BadRequest("DOCUMENTO_REQUERIDO",
                    "La verificación requiere al menos un documento.");
        }

        for (ArchivoSubido archivo : archivos) {
            if (archivo == null) {
                throw new BusinessException.BadRequest("DOCUMENTO_REQUERIDO", "El documento es obligatorio.");
            }
            ArchivoValidacion.validar(archivo.nombreOriginal(), archivo.tipoContenido(), archivo.contenido(),
                    ArchivoValidacion.DOCUMENTOS);
        }
        OffsetDateTime ahora = OffsetDateTime.now();
        SolicitudVerificacion solicitud = solicitudVerificacionRepository.guardar(new SolicitudVerificacion(
                UUID.randomUUID(), prestadorId, EstadoVerificacion.PENDIENTE, ahora, null));
        for (ArchivoSubido archivo : archivos) {
            String clave = archivoStorageService.guardar(archivo.nombreOriginal(), archivo.tipoContenido(), archivo.contenido());
            documentoVerificacionRepository.guardar(new DocumentoVerificacion(UUID.randomUUID(), solicitud.id(), clave, ahora));
        }
        return solicitud;
    }
    public SolicitudVerificacion miVerificacion(UUID prestadorId) {
        return solicitudVerificacionRepository.ultimaDePrestador(prestadorId)
                .orElseThrow(() -> new BusinessException.NotFound("SIN_VERIFICACION",
                        "No tienes solicitudes de verificación."));
    }

    public void resolver(UUID solicitudId, String estado, UUID administradorId) {
        EstadoVerificacion nuevo;
        try {
            nuevo = EstadoVerificacion.valueOf(estado);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException.BadRequest("ESTADO_INVALIDO", "Estado inválido: use APROBADA o RECHAZADA.");
        }
        if (nuevo == EstadoVerificacion.PENDIENTE) {
            throw new BusinessException.BadRequest("ESTADO_INVALIDO", "No se puede resolver como PENDIENTE.");
        }
        SolicitudVerificacion solicitud = solicitudVerificacionRepository.porId(solicitudId)
                .orElseThrow(() -> new BusinessException.NotFound("VERIFICACION_NO_ENCONTRADA",
                        "Solicitud de verificación no encontrada."));
        if (solicitud.estado() != EstadoVerificacion.PENDIENTE) {
            throw new BusinessException.Conflict("VERIFICACION_YA_RESUELTA",
                    "La solicitud ya fue resuelta.");
        }
        solicitudVerificacionRepository.guardar(new SolicitudVerificacion(solicitud.id(), solicitud.prestadorId(), nuevo,
                solicitud.fechaSolicitud(), OffsetDateTime.now()));
    }
}
