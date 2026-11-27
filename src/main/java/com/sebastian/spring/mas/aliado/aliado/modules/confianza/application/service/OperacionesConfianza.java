package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.RolConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ConfianzaConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.model.Calificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.model.EstadoVisibilidadResena;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.model.NivelVerificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.model.Reputacion;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.model.Resena;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.CalificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.ReputacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.ResenaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.SolicitudVerificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.port.ReportePort;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.ServicioLecturaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.EstadoServicio;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ConfianzaConsultaPort.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class OperacionesConfianza {
    private final CalificacionRepository calificacionesRepository;
    private final ResenaRepository resenasRepository;
    private final ReputacionRepository reputacionesRepository;
    private final SolicitudVerificacionRepository solicitudesVerificacionRepository;
    private final ServicioLecturaPort servicioLecturaPort;
    private final ReportePort reportePort;
    private final RolConsultaPort rolConsultaPort;

    public OperacionesConfianza(CalificacionRepository calificacionesRepository,
            ResenaRepository resenasRepository, ReputacionRepository reputacionesRepository,
            SolicitudVerificacionRepository solicitudesVerificacionRepository,
            ServicioLecturaPort servicioLecturaPort, ReportePort reportePort,
            RolConsultaPort rolConsultaPort) {
        this.calificacionesRepository = calificacionesRepository;
        this.resenasRepository = resenasRepository;
        this.reputacionesRepository = reputacionesRepository;
        this.solicitudesVerificacionRepository = solicitudesVerificacionRepository;
        this.servicioLecturaPort = servicioLecturaPort;
        this.reportePort = reportePort;
        this.rolConsultaPort = rolConsultaPort;
    }

    public Calificacion calificar(UUID servicioId, UUID clienteId, int valorCalificacion) {
        Calificacion calificacionRegistrada = registrarCalificacion(servicioId, clienteId, valorCalificacion);
        recalcularReputacion(calificacionRegistrada.prestadorId());
        return calificacionRegistrada;
    }

    public Calificacion registrarCalificacion(UUID servicioId, UUID clienteId, int valorCalificacion) {
        if (valorCalificacion < 1 || valorCalificacion > 5) {
            throw new BusinessException.BadRequest("VALOR_INVALIDO", "La calificación debe estar entre 1 y 5.");
        }
        var servicioContratado = servicioLecturaPort.porId(servicioId)
                .orElseThrow(() -> new BusinessException.NotFound("SERVICIO_NO_ENCONTRADO",
                        "Servicio no encontrado."));
        if (!servicioContratado.clienteId().equals(clienteId)) {
            throw new BusinessException.Forbidden("NO_AUTORIZADO",
                    "Solo el cliente del servicio puede calificarlo.");
        }
        if (servicioContratado.estado() != EstadoServicio.FINALIZADO) {
            throw new BusinessException.Conflict("SERVICIO_NO_FINALIZADO",
                    "Solo se puede calificar un servicio finalizado.");
        }
        if (calificacionesRepository.porServicioId(servicioId).isPresent()) {
            throw new BusinessException.Conflict("YA_CALIFICADO",
                    "El servicio ya fue calificado (máximo una calificación por servicio).");
        }
        Calificacion calificacion = new Calificacion(UUID.randomUUID(), servicioId, clienteId,
                servicioContratado.prestadorId(), valorCalificacion, OffsetDateTime.now());
        return calificacionesRepository.guardar(calificacion);
    }

    public void recalcularReputacion(UUID prestadorId) {
        List<Calificacion> calificacionesDelPrestador = calificacionesRepository.porPrestadorId(prestadorId);
        BigDecimal promedioCalificaciones = calificacionesDelPrestador.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.valueOf(calificacionesDelPrestador.stream()
                        .mapToInt(Calificacion::valor).average().orElse(0))
                        .setScale(2, RoundingMode.HALF_UP);
        reputacionesRepository.guardar(new Reputacion(prestadorId, promedioCalificaciones,
                calificacionesDelPrestador.size(), OffsetDateTime.now()));
    }

    public Resena agregarResena(UUID calificacionId, UUID clienteId, String textoResena) {
        Calificacion calificacion = calificacionesRepository.porId(calificacionId)
                .orElseThrow(() -> new BusinessException.NotFound("CALIFICACION_NO_ENCONTRADA",
                        "Calificación no encontrada."));
        if (!calificacion.clienteId().equals(clienteId)) {
            throw new BusinessException.Forbidden("NO_AUTORIZADO",
                    "Solo el autor de la calificación puede agregar la reseña.");
        }
        if (textoResena == null || textoResena.isBlank()) {
            throw new BusinessException.BadRequest("TEXTO_REQUERIDO", "El texto de la reseña es obligatorio.");
        }
        if (resenasRepository.porCalificacionId(calificacionId).isPresent()) {
            throw new BusinessException.Conflict("RESENA_YA_EXISTE",
                    "La calificación ya tiene una reseña (máximo una por calificación).");
        }
        return resenasRepository.guardar(new Resena(UUID.randomUUID(), calificacionId, textoResena.trim(),
                OffsetDateTime.now(), EstadoVisibilidadResena.VISIBLE));
    }

    public ConfianzaInfo confianzaDe(UUID prestadorId) {
        if (!rolConsultaPort.esPrestador(prestadorId)) {
            throw new BusinessException.NotFound("PRESTADOR_NO_ENCONTRADO", "Prestador no encontrado.");
        }
        Reputacion reputacion = reputacionesRepository.porPrestadorId(prestadorId).orElse(null);
        NivelVerificacion nivelVerificacion =
                solicitudesVerificacionRepository.contarAprobadasDePrestador(prestadorId) > 0
                ? NivelVerificacion.BASICO : NivelVerificacion.SIN_VERIFICAR;
        return new ConfianzaInfo(
                new ReputacionInfo(reputacion == null ? BigDecimal.ZERO : reputacion.valorAgregado(),
                        reputacion == null ? 0 : reputacion.cantidadCalificaciones()),
                nivelVerificacion);
    }

    public List<ResenaPublica> resenasVisiblesDe(UUID prestadorId) {
        if (!rolConsultaPort.esPrestador(prestadorId)) {
            throw new BusinessException.NotFound("PRESTADOR_NO_ENCONTRADO", "Prestador no encontrado.");
        }
        return calificacionesRepository.porPrestadorId(prestadorId).stream()
                .flatMap(calificacion -> resenasRepository.porCalificacionId(calificacion.id()).stream()
                        .filter(resena -> resena.estadoVisibilidad() == EstadoVisibilidadResena.VISIBLE)
                        .map(resena -> new ResenaPublica(resena.id(), calificacion.valor(),
                                resena.texto(), resena.fechaCreacion())))
                .sorted(Comparator.comparing(ResenaPublica::fechaCreacion).reversed())
                .toList();
    }
    public UUID reportarResena(UUID resenaId, UUID reportanteId, String motivoReporte) {
        Resena resena = resenasRepository.porId(resenaId)
                .orElseThrow(() -> new BusinessException.NotFound("RESENA_NO_ENCONTRADA",
                        "Reseña no encontrada."));
        if (resena.estadoVisibilidad() == EstadoVisibilidadResena.OCULTA) {
            throw new BusinessException.Conflict("RESENA_OCULTA",
                    "No se puede reportar una reseña oculta.");
        }
        return reportePort.crearReporteResena(resenaId, reportanteId, motivoReporte);
    }

    public void ocultarResena(UUID resenaId) {
        Resena resena = resenasRepository.porId(resenaId)
                .orElseThrow(() -> new BusinessException.NotFound("RESENA_NO_ENCONTRADA",
                        "Reseña no encontrada."));
        resenasRepository.guardar(new Resena(resena.id(), resena.calificacionId(), resena.texto(),
                resena.fechaCreacion(), EstadoVisibilidadResena.OCULTA));
    }
}
