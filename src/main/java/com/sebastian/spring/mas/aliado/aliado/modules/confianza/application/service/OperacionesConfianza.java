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
    private final CalificacionRepository calificaciones;
    private final ResenaRepository resenas;
    private final ReputacionRepository reputaciones;
    private final SolicitudVerificacionRepository verificaciones;
    private final ServicioLecturaPort servicios;
    private final ReportePort reportes;
    private final RolConsultaPort roles;

    public OperacionesConfianza(CalificacionRepository calificaciones, ResenaRepository resenas,
                            ReputacionRepository reputaciones,
                            SolicitudVerificacionRepository verificaciones,
                            ServicioLecturaPort servicios, ReportePort reportes,
                            RolConsultaPort roles) {
        this.calificaciones = calificaciones;
        this.resenas = resenas;
        this.reputaciones = reputaciones;
        this.verificaciones = verificaciones;
        this.servicios = servicios;
        this.reportes = reportes;
        this.roles = roles;
    }
    public Calificacion calificar(UUID servicioId, UUID clienteId, int valor) {
        Calificacion resultado = registrarCalificacion(servicioId, clienteId, valor);
        recalcularReputacion(resultado.prestadorId());
        return resultado;
    }

    public Calificacion registrarCalificacion(UUID servicioId, UUID clienteId, int valor) {
        if (valor < 1 || valor > 5) {
            throw new BusinessException.BadRequest("VALOR_INVALIDO", "La calificación debe estar entre 1 y 5.");
        }
        var servicio = servicios.porId(servicioId)
                .orElseThrow(() -> new BusinessException.NotFound("SERVICIO_NO_ENCONTRADO",
                        "Servicio no encontrado."));
        if (!servicio.clienteId().equals(clienteId)) {
            throw new BusinessException.Forbidden("NO_AUTORIZADO",
                    "Solo el cliente del servicio puede calificarlo.");
        }
        if (servicio.estado() != EstadoServicio.FINALIZADO) {
            throw new BusinessException.Conflict("SERVICIO_NO_FINALIZADO",
                    "Solo se puede calificar un servicio finalizado.");
        }
        if (calificaciones.porServicioId(servicioId).isPresent()) {
            throw new BusinessException.Conflict("YA_CALIFICADO",
                    "El servicio ya fue calificado (máximo una calificación por servicio).");
        }
        Calificacion calificacion = new Calificacion(UUID.randomUUID(), servicioId, clienteId,
                servicio.prestadorId(), valor, OffsetDateTime.now());
        Calificacion guardada = calificaciones.guardar(calificacion);
        return guardada;
    }

    public void recalcularReputacion(UUID prestadorId) {
        List<Calificacion> todas = calificaciones.porPrestadorId(prestadorId);
        BigDecimal promedio = todas.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.valueOf(todas.stream().mapToInt(Calificacion::valor).average().orElse(0))
                        .setScale(2, RoundingMode.HALF_UP);
        reputaciones.guardar(new Reputacion(prestadorId, promedio, todas.size(), OffsetDateTime.now()));
    }
    public Resena agregarResena(UUID calificacionId, UUID clienteId, String texto) {
        Calificacion calificacion = calificaciones.porId(calificacionId)
                .orElseThrow(() -> new BusinessException.NotFound("CALIFICACION_NO_ENCONTRADA",
                        "Calificación no encontrada."));
        if (!calificacion.clienteId().equals(clienteId)) {
            throw new BusinessException.Forbidden("NO_AUTORIZADO",
                    "Solo el autor de la calificación puede agregar la reseña.");
        }
        if (texto == null || texto.isBlank()) {
            throw new BusinessException.BadRequest("TEXTO_REQUERIDO", "El texto de la reseña es obligatorio.");
        }
        if (resenas.porCalificacionId(calificacionId).isPresent()) {
            throw new BusinessException.Conflict("RESENA_YA_EXISTE",
                    "La calificación ya tiene una reseña (máximo una por calificación).");
        }
        return resenas.guardar(new Resena(UUID.randomUUID(), calificacionId, texto.trim(),
                OffsetDateTime.now(), EstadoVisibilidadResena.VISIBLE));
    }

    public ConfianzaInfo confianzaDe(UUID prestadorId) {
        if (!roles.esPrestador(prestadorId)) {
            throw new BusinessException.NotFound("PRESTADOR_NO_ENCONTRADO", "Prestador no encontrado.");
        }
        Reputacion reputacion = reputaciones.porPrestadorId(prestadorId).orElse(null);
        NivelVerificacion nivel = verificaciones.contarAprobadasDePrestador(prestadorId) > 0
                ? NivelVerificacion.BASICO : NivelVerificacion.SIN_VERIFICAR;
        return new ConfianzaInfo(
                new ReputacionInfo(reputacion == null ? BigDecimal.ZERO : reputacion.valorAgregado(),
                        reputacion == null ? 0 : reputacion.cantidadCalificaciones()),
                nivel);
    }

    public List<ResenaPublica> resenasVisiblesDe(UUID prestadorId) {
        if (!roles.esPrestador(prestadorId)) {
            throw new BusinessException.NotFound("PRESTADOR_NO_ENCONTRADO", "Prestador no encontrado.");
        }
        return calificaciones.porPrestadorId(prestadorId).stream()
                .flatMap(calificacion -> resenas.porCalificacionId(calificacion.id()).stream()
                        .filter(resena -> resena.estadoVisibilidad() == EstadoVisibilidadResena.VISIBLE)
                        .map(resena -> new ResenaPublica(resena.id(), calificacion.valor(),
                                resena.texto(), resena.fechaCreacion())))
                .sorted(Comparator.comparing(ResenaPublica::fechaCreacion).reversed())
                .toList();
    }
    public UUID reportarResena(UUID resenaId, UUID reportanteId, String motivo) {
        Resena resena = resenas.porId(resenaId)
                .orElseThrow(() -> new BusinessException.NotFound("RESENA_NO_ENCONTRADA",
                        "Reseña no encontrada."));
        if (resena.estadoVisibilidad() == EstadoVisibilidadResena.OCULTA) {
            throw new BusinessException.Conflict("RESENA_OCULTA",
                    "No se puede reportar una reseña oculta.");
        }
        return reportes.crearReporteResena(resenaId, reportanteId, motivo);
    }

    public void ocultarResena(UUID resenaId) {
        Resena resena = resenas.porId(resenaId)
                .orElseThrow(() -> new BusinessException.NotFound("RESENA_NO_ENCONTRADA",
                        "Reseña no encontrada."));
        resenas.guardar(new Resena(resena.id(), resena.calificacionId(), resena.texto(),
                resena.fechaCreacion(), EstadoVisibilidadResena.OCULTA));
    }
}
