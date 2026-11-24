package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Perfil profesional del Prestador (SPEC-05). datosGenerales se guarda como JSON hasta cerrarlo (V3). */
public record PerfilProfesional(UUID id, UUID prestadorId, Map<String, Object> datosGenerales,
                                EstadoExposicion estadoExposicion, OffsetDateTime fechaCreacion,
                                OffsetDateTime fechaActualizacion, Set<UUID> categoriaIds, Set<UUID> zonaIds,
                                List<ExperienciaProfesional> experiencias, List<ServicioPortafolio> portafolio,
                                List<Disponibilidad> disponibilidades, List<CompromisoAgenda> compromisos) {

    /**
     * BE-05: el contrato MVP de datos generales solo publica la presentación libre.
     * Teléfono, correo, dirección exacta, documentos y claves heredadas no salen en la vista pública.
     */
    public static final Set<String> DATOS_PUBLICOS = Set.of("descripcion");

    /** Filtra datosGenerales a la presentación pública aprobada; lo demás nunca se expone. */
    public static Map<String, Object> datosPublicos(Map<String, Object> datos) {
        if (datos == null) {
            return Map.of();
        }
        Map<String, Object> filtrados = new java.util.LinkedHashMap<>();
        datos.forEach((clave, valor) -> {
            if (DATOS_PUBLICOS.contains(clave) && valor instanceof String texto && !texto.isBlank()) {
                filtrados.put(clave, texto);
            }
        });
        return java.util.Collections.unmodifiableMap(filtrados);
    }

    public PerfilProfesional conExposicion(EstadoExposicion nueva) {
        return new PerfilProfesional(id, prestadorId, datosGenerales, nueva, fechaCreacion,
                OffsetDateTime.now(), categoriaIds, zonaIds, experiencias, portafolio, disponibilidades, compromisos);
    }

    public PerfilProfesional conCategorias(Set<UUID> nuevas) {
        return new PerfilProfesional(id, prestadorId, datosGenerales, estadoExposicion, fechaCreacion,
                OffsetDateTime.now(), nuevas, zonaIds, experiencias, portafolio, disponibilidades, compromisos);
    }

    public PerfilProfesional conZonas(Set<UUID> nuevas) {
        return new PerfilProfesional(id, prestadorId, datosGenerales, estadoExposicion, fechaCreacion,
                OffsetDateTime.now(), categoriaIds, nuevas, experiencias, portafolio, disponibilidades, compromisos);
    }

    public PerfilProfesional conDatos(Map<String, Object> nuevos) {
        return new PerfilProfesional(id, prestadorId, nuevos, estadoExposicion, fechaCreacion,
                OffsetDateTime.now(), categoriaIds, zonaIds, experiencias, portafolio, disponibilidades, compromisos);
    }

    public PerfilProfesional conExperiencias(List<ExperienciaProfesional> nuevas) {
        return new PerfilProfesional(id, prestadorId, datosGenerales, estadoExposicion, fechaCreacion,
                OffsetDateTime.now(), categoriaIds, zonaIds, nuevas, portafolio, disponibilidades, compromisos);
    }

    public PerfilProfesional conPortafolio(List<ServicioPortafolio> nuevo) {
        return new PerfilProfesional(id, prestadorId, datosGenerales, estadoExposicion, fechaCreacion,
                OffsetDateTime.now(), categoriaIds, zonaIds, experiencias, nuevo, disponibilidades, compromisos);
    }

    public PerfilProfesional conDisponibilidades(List<Disponibilidad> nuevas) {
        return new PerfilProfesional(id, prestadorId, datosGenerales, estadoExposicion, fechaCreacion,
                OffsetDateTime.now(), categoriaIds, zonaIds, experiencias, portafolio, nuevas, compromisos);
    }

    public PerfilProfesional conCompromiso(CompromisoAgenda compromiso) {
        var lista = new ArrayList<>(compromisos);
        lista.removeIf(c -> c.id().equals(compromiso.id()));
        lista.add(compromiso);
        return new PerfilProfesional(id, prestadorId, datosGenerales, estadoExposicion, fechaCreacion,
                OffsetDateTime.now(), categoriaIds, zonaIds, experiencias, portafolio, disponibilidades, List.copyOf(lista));
    }

    public PerfilProfesional sinPortafolio(UUID servicioId) {
        return new PerfilProfesional(id, prestadorId, datosGenerales, estadoExposicion, fechaCreacion,
                OffsetDateTime.now(), categoriaIds, zonaIds, experiencias,
                portafolio.stream().filter(s -> !s.id().equals(servicioId)).toList(),
                disponibilidades, compromisos);
    }
}
