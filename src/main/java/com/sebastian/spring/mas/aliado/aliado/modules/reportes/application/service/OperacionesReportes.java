package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoStorageService;
import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoValidacion;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.UsuarioConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ResenaConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.model.EstadoReporte;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.model.EvidenciaReporte;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.model.Reporte;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.model.TipoReporte;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.repository.EvidenciaReporteRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.repository.ReporteRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.ServicioLecturaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.EvidenciaContenido;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
/**
 * Reportes y evidencias (SPEC-11, UC065–UC067).
 * Mismo reportante + mismo objetivo + mismo motivo + reporte abierto se consolida
 * en el existente (no se crea un duplicado). Todo reporte va a la cola de moderación.
 */
public class OperacionesReportes {
    private final ReporteRepository reportes;
    private final EvidenciaReporteRepository evidencias;
    private final ResenaConsultaPort resenas;
    private final UsuarioConsultaPort usuarios;
    private final ServicioLecturaPort servicios;
    private final ArchivoStorageService storage;

    public OperacionesReportes(ReporteRepository reportes, EvidenciaReporteRepository evidencias,
                          ResenaConsultaPort resenas, UsuarioConsultaPort usuarios,
                          ServicioLecturaPort servicios, ArchivoStorageService storage) {
        this.reportes = reportes;
        this.evidencias = evidencias;
        this.resenas = resenas;
        this.usuarios = usuarios;
        this.servicios = servicios;
        this.storage = storage;
    }

    private UUID crear(TipoReporte tipo, UUID objetivoId, UUID reportanteId, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new BusinessException.BadRequest("MOTIVO_REQUERIDO", "El motivo es obligatorio.");
        }
        if (motivo.length() > 500) {
            throw new BusinessException.BadRequest("MOTIVO_MUY_LARGO", "El motivo admite máximo 500 caracteres.");
        }
        var existente = reportes.abiertoEquivalente(reportanteId, tipo.name(), objetivoId, motivo.trim());
        if (existente.isPresent()) {
            return existente.get().id();
        }
        OffsetDateTime ahora = OffsetDateTime.now();
        Reporte reporte = new Reporte(UUID.randomUUID(), tipo, reportanteId,
                tipo == TipoReporte.RESENA ? objetivoId : null,
                tipo == TipoReporte.USUARIO ? objetivoId : null,
                tipo == TipoReporte.SERVICIO ? objetivoId : null,
                motivo.trim(), EstadoReporte.PENDIENTE, null, null, null, ahora, null, 0);
        return reportes.guardar(reporte).id();
    }

    public UUID crearReporteResena(UUID resenaId, UUID reportanteId, String motivo) {
        if (!resenas.existe(resenaId)) {
            throw new BusinessException.NotFound("RESENA_NO_ENCONTRADA", "Reseña no encontrada.");
        }
        return crear(TipoReporte.RESENA, resenaId, reportanteId, motivo);
    }

    public UUID crearReporteUsuario(UUID usuarioObjetivoId, UUID reportanteId, String motivo) {
        if (!usuarios.existe(usuarioObjetivoId)) {
            throw new BusinessException.NotFound("USUARIO_NO_ENCONTRADO", "Usuario no encontrado.");
        }
        if (usuarioObjetivoId.equals(reportanteId)) {
            throw new BusinessException.BadRequest("AUTO_REPORTE", "No puedes reportarte a ti mismo.");
        }
        return crear(TipoReporte.USUARIO, usuarioObjetivoId, reportanteId, motivo);
    }

    public UUID crearReporteServicio(UUID servicioId, UUID reportanteId, String motivo) {
        var servicio = servicios.porId(servicioId)
                .orElseThrow(() -> new BusinessException.NotFound("SERVICIO_NO_ENCONTRADO",
                        "Servicio no encontrado."));
        if (!servicio.esParticipante(reportanteId)) {
            throw new BusinessException.Forbidden("NO_PARTICIPANTE",
                    "Solo un participante del servicio puede reportarlo.");
        }
        return crear(TipoReporte.SERVICIO, servicioId, reportanteId, motivo);
    }
    public EvidenciaReporte agregarEvidencia(UUID reporteId, UUID reportanteId, String nombreOriginal,
                                             String tipoContenido, byte[] contenido) {
        Reporte reporte = reportes.porIdParaActualizar(reporteId)
                .orElseThrow(() -> new BusinessException.NotFound("REPORTE_NO_ENCONTRADO",
                        "Reporte no encontrado."));
        if (!reporte.reportanteId().equals(reportanteId)) {
            throw new BusinessException.Forbidden("NO_AUTORIZADO",
                    "Solo el reportante puede adjuntar evidencia a su reporte.");
        }
        if (!reporte.abierto()) {
            throw new BusinessException.Conflict("REPORTE_CERRADO",
                    "El reporte ya está cerrado; no admite más evidencia.");
        }
        ArchivoValidacion.validar(nombreOriginal, tipoContenido, contenido, ArchivoValidacion.DOCUMENTOS);
        String clave = storage.guardar(nombreOriginal, tipoContenido, contenido);
        return evidencias.guardar(new EvidenciaReporte(UUID.randomUUID(), reporteId, clave,
                tipoContenido, OffsetDateTime.now()));
    }
    public Reporte miReporte(UUID reporteId, UUID reportanteId) {
        Reporte reporte = reportes.porId(reporteId)
                .orElseThrow(() -> new BusinessException.NotFound("REPORTE_NO_ENCONTRADO",
                        "Reporte no encontrado."));
        if (!reporte.reportanteId().equals(reportanteId)) {
            throw new BusinessException.Forbidden("NO_AUTORIZADO", "El reporte no te pertenece.");
        }
        return reporte;
    }
    public List<EvidenciaReporte> evidenciasDe(UUID reporteId, UUID reportanteId) {
        miReporte(reporteId, reportanteId);
        return evidencias.porReporte(reporteId);
    }

    public List<EvidenciaReporte> evidenciasDe(UUID reporteId) {
        return evidencias.porReporte(reporteId);
    }

    /** Descarga de evidencia para el moderador (sin chequeo de propiedad: rol admin). */
    public EvidenciaContenido descargarEvidencia(UUID reporteId, UUID evidenciaId) {
        EvidenciaReporte evidencia = evidencias.porReporte(reporteId).stream()
                .filter(e -> e.id().equals(evidenciaId))
                .findFirst()
                .orElseThrow(() -> new BusinessException.NotFound("EVIDENCIA_NO_ENCONTRADA",
                        "Evidencia no encontrada."));
        return new EvidenciaContenido(storage.leer(evidencia.archivoKey()), evidencia.tipoContenido());
    }

    /** Descarga de evidencia para el reportante (con chequeo de propiedad). */
    public EvidenciaContenido descargarEvidenciaPropia(UUID reporteId, UUID evidenciaId, UUID reportanteId) {
        miReporte(reporteId, reportanteId);
        return descargarEvidencia(reporteId, evidenciaId);
    }

    public List<Reporte> reportesDe(UUID reportanteId) {
        return reportes.porReportante(reportanteId);
    }

    public List<Reporte> cola(String estado) {
        return reportes.cola(estado);
    }

    public Reporte marcarRevisionDetalle(UUID reporteId, UUID administradorId) {
        Reporte reporte = reportes.porIdParaActualizar(reporteId)
                .orElseThrow(() -> new BusinessException.NotFound("REPORTE_NO_ENCONTRADO",
                        "Reporte no encontrado."));
        // La máquina de estados no admite reabrir desde un estado terminal (SPEC-12).
        if (!reporte.abierto()) {
            throw new BusinessException.Conflict("REPORTE_CERRADO",
                    "El reporte ya está cerrado; no se puede reabrir.");
        }
        Reporte actualizado = new Reporte(reporte.id(), reporte.tipo(), reporte.reportanteId(),
                reporte.resenaId(), reporte.usuarioObjetivoId(), reporte.servicioId(), reporte.motivo(),
                EstadoReporte.EN_REVISION, OffsetDateTime.now(), reporte.justificacionResolucion(),
                reporte.revisadoPor(), reporte.fechaCreacion(), reporte.fechaResolucion(), reporte.version());
        return reportes.guardar(actualizado);
    }

    public Reporte resolver(UUID reporteId, UUID administradorId, String resultado, String justificacion) {
        Reporte reporte = reportes.porIdParaActualizar(reporteId)
                .orElseThrow(() -> new BusinessException.NotFound("REPORTE_NO_ENCONTRADO",
                        "Reporte no encontrado."));
        // Un reporte en estado terminal no se puede re-resolver (SPEC-12).
        if (!reporte.abierto()) {
            throw new BusinessException.Conflict("REPORTE_CERRADO",
                    "El reporte ya está cerrado; no se puede resolver de nuevo.");
        }
        EstadoReporte nuevo;
        try {
            nuevo = EstadoReporte.valueOf(resultado);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException.BadRequest("RESULTADO_INVALIDO",
                    "El resultado debe ser RESUELTO o DESCARTADO.");
        }
        if (nuevo != EstadoReporte.RESUELTO && nuevo != EstadoReporte.DESCARTADO) {
            throw new BusinessException.BadRequest("RESULTADO_INVALIDO",
                    "El resultado debe ser RESUELTO o DESCARTADO.");
        }
        if (reporte.detalleRevisadoEn() == null) {
            throw new BusinessException.Conflict("SIN_REVISION_PREVIA",
                    "No se puede resolver sin haber abierto antes el detalle del reporte.");
        }
        if (justificacion == null || justificacion.isBlank()) {
            throw new BusinessException.BadRequest("JUSTIFICACION_REQUERIDA",
                    "La resolución exige una justificación.");
        }
        Reporte resuelto = new Reporte(reporte.id(), reporte.tipo(), reporte.reportanteId(),
                reporte.resenaId(), reporte.usuarioObjetivoId(), reporte.servicioId(), reporte.motivo(),
                nuevo, reporte.detalleRevisadoEn(), justificacion.trim(), administradorId,
                reporte.fechaCreacion(), OffsetDateTime.now(), reporte.version());
        return reportes.guardar(resuelto);
    }
}
