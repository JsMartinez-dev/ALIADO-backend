package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto;

import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Datos del perfil propio; no es el contrato público de descubrimiento. */
public record PerfilProfesionalResult(UUID id, UUID prestadorId, Map<String, Object> datosGenerales,
        String estadoExposicion, List<UUID> categoriaIds, List<UUID> zonaIds,
        List<ExperienciaResult> experiencias, List<PortafolioResult> portafolio,
        List<DisponibilidadResult> disponibilidades, List<CompromisoResult> compromisos) {
    public record ExperienciaResult(UUID id, String descripcion, LocalDate fechaInicio, LocalDate fechaFin) {}
    public record PortafolioResult(UUID id, String descripcion, String evidenciaKey, OffsetDateTime fechaCreacion) {}
    public record DisponibilidadResult(UUID id, Integer diaSemana, LocalTime horaInicio, LocalTime horaFin) {}
    public record CompromisoResult(UUID id, OffsetDateTime fechaHoraInicio, OffsetDateTime fechaHoraFin, String origen) {}

    public static PerfilProfesionalResult desde(PerfilProfesional perfil) {
        return new PerfilProfesionalResult(perfil.id(), perfil.prestadorId(), perfil.datosGenerales(),
                perfil.estadoExposicion().name(), List.copyOf(perfil.categoriaIds()), List.copyOf(perfil.zonaIds()),
                perfil.experiencias().stream().map(e -> new ExperienciaResult(
                        e.id(), e.descripcion(), e.fechaInicio(), e.fechaFin())).toList(),
                perfil.portafolio().stream().map(p -> new PortafolioResult(
                        p.id(), p.descripcion(), p.evidenciaKey(), p.fechaCreacion())).toList(),
                perfil.disponibilidades().stream().map(d -> new DisponibilidadResult(
                        d.id(), d.diaSemana(), d.horaInicio(), d.horaFin())).toList(),
                perfil.compromisos().stream().map(c -> new CompromisoResult(
                        c.id(), c.fechaHoraInicio(), c.fechaHoraFin(), c.origen().name())).toList());
    }
}
