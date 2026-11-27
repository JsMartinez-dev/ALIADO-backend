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

public class OperacionesReportes {
    private final ReporteRepository reportesRepository;
    private final EvidenciaReporteRepository evidenciasReporteRepository;
    private final ResenaConsultaPort resenaConsultaPort;
    private final UsuarioConsultaPort usuarioConsultaPort;
    private final ServicioLecturaPort servicioLecturaPort;
    private final ArchivoStorageService archivoStorageService;

    public OperacionesReportes(ReporteRepository reportesRepository,
            EvidenciaReporteRepository evidenciasReporteRepository,
            ResenaConsultaPort resenaConsultaPort, UsuarioConsultaPort usuarioConsultaPort,
            ServicioLecturaPort servicioLecturaPort, ArchivoStorageService archivoStorageService) {
        this.reportesRepository = reportesRepository;
        this.evidenciasReporteRepository = evidenciasReporteRepository;
        this.resenaConsultaPort = resenaConsultaPort;
        this.usuarioConsultaPort = usuarioConsultaPort;
        this.servicioLecturaPort = servicioLecturaPort;
        this.archivoStorageService = archivoStorageService;
    }

    private UUID crear(TipoReporte tipoReporte, UUID objetivoId, UUID reportanteId, String motivoReporte) {
        if (motivoReporte == null || motivoReporte.isBlank()) {
            throw new BusinessException.BadRequest("MOTIVO_REQUERIDO", "El motivo es obligatorio.");
        }
        if (motivoReporte.length() > 500) {
            throw new BusinessException.BadRequest("MOTIVO_MUY_LARGO", "El motivo admite máximo 500 caracteres.");
        }
        var reporteEquivalente = reportesRepository.abiertoEquivalente(reportanteId, tipoReporte.name(), objetivoId, motivoReporte.trim());
        if (reporteEquivalente.isPresent()) {
            return reporteEquivalente.get().id();
        }
        OffsetDateTime fechaHoraCreacion = OffsetDateTime.now();
        Reporte reporteNuevo = new Reporte(UUID.randomUUID(), tipoReporte, reportanteId,
                tipoReporte == TipoReporte.RESENA ? objetivoId : null,
                tipoReporte == TipoReporte.USUARIO ? objetivoId : null,
                tipoReporte == TipoReporte.SERVICIO ? objetivoId : null,
                motivoReporte.trim(), EstadoReporte.PENDIENTE, null, null, null, fechaHoraCreacion, null, 0);
        return reportesRepository.guardar(reporteNuevo).id();
    }

    public UUID crearReporteResena(UUID resenaId, UUID reportanteId, String motivoReporte) {
        if (!resenaConsultaPort.existe(resenaId)) {
            throw new BusinessException.NotFound("RESENA_NO_ENCONTRADA", "Reseña no encontrada.");
        }
        return crear(TipoReporte.RESENA, resenaId, reportanteId, motivoReporte);
    }

    public UUID crearReporteUsuario(UUID usuarioObjetivoId, UUID reportanteId, String motivoReporte) {
        if (!usuarioConsultaPort.existe(usuarioObjetivoId)) {
            throw new BusinessException.NotFound("USUARIO_NO_ENCONTRADO", "Usuario no encontrado.");
        }
        if (usuarioObjetivoId.equals(reportanteId)) {
            throw new BusinessException.BadRequest("AUTO_REPORTE", "No puedes reportarte a ti mismo.");
        }
        return crear(TipoReporte.USUARIO, usuarioObjetivoId, reportanteId, motivoReporte);
    }

    public UUID crearReporteServicio(UUID servicioId, UUID reportanteId, String motivoReporte) {
        var servicioContratado = servicioLecturaPort.porId(servicioId)
                .orElseThrow(() -> new BusinessException.NotFound("SERVICIO_NO_ENCONTRADO",
                        "Servicio no encontrado."));
        if (!servicioContratado.esParticipante(reportanteId)) {
            throw new BusinessException.Forbidden("NO_PARTICIPANTE",
                    "Solo un participante del servicio puede reportarlo.");
        }
        return crear(TipoReporte.SERVICIO, servicioId, reportanteId, motivoReporte);
    }

    public EvidenciaReporte agregarEvidencia(UUID reporteId, UUID reportanteId, String nombreOriginal,
            String tipoContenido, byte[] contenidoArchivo) {
        Reporte reporte = reportesRepository.porIdParaActualizar(reporteId)
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
        ArchivoValidacion.validar(nombreOriginal, tipoContenido, contenidoArchivo, ArchivoValidacion.DOCUMENTOS);
        String claveArchivo = archivoStorageService.guardar(nombreOriginal, tipoContenido, contenidoArchivo);
        return evidenciasReporteRepository.guardar(new EvidenciaReporte(UUID.randomUUID(), reporteId, claveArchivo,
                tipoContenido, OffsetDateTime.now()));
    }

    public Reporte miReporte(UUID reporteId, UUID reportanteId) {
        Reporte reporte = reportesRepository.porId(reporteId)
                .orElseThrow(() -> new BusinessException.NotFound("REPORTE_NO_ENCONTRADO",
                        "Reporte no encontrado."));
        if (!reporte.reportanteId().equals(reportanteId)) {
            throw new BusinessException.Forbidden("NO_AUTORIZADO", "El reporte no te pertenece.");
        }
        return reporte;
    }
    public List<EvidenciaReporte> evidenciasDe(UUID reporteId, UUID reportanteId) {
        miReporte(reporteId, reportanteId);
        return evidenciasReporteRepository.porReporte(reporteId);
    }

    public List<EvidenciaReporte> evidenciasDe(UUID reporteId) {
        return evidenciasReporteRepository.porReporte(reporteId);
    }

    /** Descarga de evidencia para el moderador (sin chequeo de propiedad: rol admin). */
    public EvidenciaContenido descargarEvidencia(UUID reporteId, UUID evidenciaId) {
        EvidenciaReporte evidenciaReporte = evidenciasReporteRepository.porReporte(reporteId).stream()
                .filter(evidencia -> evidencia.id().equals(evidenciaId))
                .findFirst()
                .orElseThrow(() -> new BusinessException.NotFound("EVIDENCIA_NO_ENCONTRADA",
                        "Evidencia no encontrada."));
        return new EvidenciaContenido(
                archivoStorageService.leer(evidenciaReporte.archivoKey()), evidenciaReporte.tipoContenido());
    }

    /** Descarga de evidencia para el reportante (con chequeo de propiedad). */
    public EvidenciaContenido descargarEvidenciaPropia(UUID reporteId, UUID evidenciaId, UUID reportanteId) {
        miReporte(reporteId, reportanteId);
        return descargarEvidencia(reporteId, evidenciaId);
    }

    public List<Reporte> cola(String estadoReporte) {
        return reportesRepository.cola(estadoReporte);
    }

    public Reporte marcarRevisionDetalle(UUID reporteId, UUID administradorId) {
        Reporte reporte = reportesRepository.porIdParaActualizar(reporteId)
                .orElseThrow(() -> new BusinessException.NotFound("REPORTE_NO_ENCONTRADO",
                        "Reporte no encontrado."));
        // La máquina de estados no admite reabrir desde un estado terminal (SPEC-12).
        if (!reporte.abierto()) {
            throw new BusinessException.Conflict("REPORTE_CERRADO",
                    "El reporte ya está cerrado; no se puede reabrir.");
        }
        Reporte reporteActualizado = new Reporte(reporte.id(), reporte.tipo(), reporte.reportanteId(),
                reporte.resenaId(), reporte.usuarioObjetivoId(), reporte.servicioId(), reporte.motivo(),
                EstadoReporte.EN_REVISION, OffsetDateTime.now(), reporte.justificacionResolucion(),
                reporte.revisadoPor(), reporte.fechaCreacion(), reporte.fechaResolucion(), reporte.version());
        return reportesRepository.guardar(reporteActualizado);
    }

    public Reporte resolver(UUID reporteId, UUID administradorId, String resultadoResolucion,
            String justificacionResolucion) {
        Reporte reporte = reportesRepository.porIdParaActualizar(reporteId)
                .orElseThrow(() -> new BusinessException.NotFound("REPORTE_NO_ENCONTRADO",
                        "Reporte no encontrado."));
        // Un reporte en estado terminal no se puede re-resolver (SPEC-12).
        if (!reporte.abierto()) {
            throw new BusinessException.Conflict("REPORTE_CERRADO",
                    "El reporte ya está cerrado; no se puede resolver de nuevo.");
        }
        EstadoReporte nuevoEstadoReporte;
        try {
            nuevoEstadoReporte = EstadoReporte.valueOf(resultadoResolucion);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException.BadRequest("RESULTADO_INVALIDO",
                    "El resultado debe ser RESUELTO o DESCARTADO.");
        }
        if (nuevoEstadoReporte != EstadoReporte.RESUELTO && nuevoEstadoReporte != EstadoReporte.DESCARTADO) {
            throw new BusinessException.BadRequest("RESULTADO_INVALIDO",
                    "El resultado debe ser RESUELTO o DESCARTADO.");
        }
        if (reporte.detalleRevisadoEn() == null) {
            throw new BusinessException.Conflict("SIN_REVISION_PREVIA",
                    "No se puede resolver sin haber abierto antes el detalle del reporte.");
        }
        if (justificacionResolucion == null || justificacionResolucion.isBlank()) {
            throw new BusinessException.BadRequest("JUSTIFICACION_REQUERIDA",
                    "La resolución exige una justificación.");
        }
        Reporte reporteResuelto = new Reporte(reporte.id(), reporte.tipo(), reporte.reportanteId(),
                reporte.resenaId(), reporte.usuarioObjetivoId(), reporte.servicioId(), reporte.motivo(),
                nuevoEstadoReporte, reporte.detalleRevisadoEn(), justificacionResolucion.trim(), administradorId,
                reporte.fechaCreacion(), OffsetDateTime.now(), reporte.version());
        return reportesRepository.guardar(reporteResuelto);
    }
}
