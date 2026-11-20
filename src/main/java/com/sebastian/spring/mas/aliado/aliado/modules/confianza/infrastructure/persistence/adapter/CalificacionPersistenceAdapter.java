package com.sebastian.spring.mas.aliado.aliado.modules.confianza.infrastructure.persistence.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.confianza.infrastructure.persistence.mapper.ConfianzaPersistenceMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.model.Calificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.CalificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.infrastructure.persistence.jpa.CalificacionJpaRepository;

/** Adaptador de calificaciones (SPEC-10). */
@Component
public class CalificacionPersistenceAdapter implements CalificacionRepository {
    private final CalificacionJpaRepository calificaciones;

    public CalificacionPersistenceAdapter(CalificacionJpaRepository calificaciones) {
        this.calificaciones = calificaciones;
    }

    @Override
    public Optional<Calificacion> porId(UUID id) {
        return calificaciones.findById(id).map(ConfianzaPersistenceMapper::aDominio);
    }

    @Override
    public Optional<Calificacion> porServicioId(UUID servicioId) {
        return calificaciones.findByServicioId(servicioId).map(ConfianzaPersistenceMapper::aDominio);
    }

    @Override
    public List<Calificacion> porPrestadorId(UUID prestadorId) {
        return calificaciones.findByPrestadorId(prestadorId).stream()
                .map(ConfianzaPersistenceMapper::aDominio).toList();
    }

    @Override
    public List<Calificacion> porClienteId(UUID clienteId) {
        return calificaciones.findByClienteIdOrderByFechaDesc(clienteId).stream()
                .map(ConfianzaPersistenceMapper::aDominio).toList();
    }

    @Override
    public Calificacion guardar(Calificacion calificacion) {
        return ConfianzaPersistenceMapper.aDominio(calificaciones.save(ConfianzaPersistenceMapper.aEntidad(calificacion)));
    }
}
