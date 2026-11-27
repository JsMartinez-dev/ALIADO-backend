package com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository;

import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.model.Calificacion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CalificacionRepository {
    Optional<Calificacion> porId(UUID id);
    Optional<Calificacion> porServicioId(UUID servicioId);
    List<Calificacion> porPrestadorId(UUID prestadorId);
    List<Calificacion> porClienteId(UUID clienteId);
    Calificacion guardar(Calificacion calificacion);
}
