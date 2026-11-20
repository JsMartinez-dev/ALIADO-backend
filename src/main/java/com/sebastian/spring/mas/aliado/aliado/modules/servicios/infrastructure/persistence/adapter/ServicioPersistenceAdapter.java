package com.sebastian.spring.mas.aliado.aliado.modules.servicios.infrastructure.persistence.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.ServicioContratado;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.repository.ServicioRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.infrastructure.persistence.jpa.ServicioJpaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.infrastructure.persistence.mapper.ServicioPersistenceMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class ServicioPersistenceAdapter implements ServicioRepository {
    private final ServicioJpaRepository jpa;

    public ServicioPersistenceAdapter(ServicioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<ServicioContratado> porId(UUID id) {
        return jpa.findById(id).map(ServicioPersistenceMapper::aDominio);
    }

    @Override
    public Optional<ServicioContratado> porSolicitudId(UUID solicitudId) {
        return jpa.findBySolicitudId(solicitudId).map(ServicioPersistenceMapper::aDominio);
    }

    @Override
    public List<ServicioContratado> porUsuario(UUID usuarioId) {
        return jpa.findByUsuario(usuarioId).stream().map(ServicioPersistenceMapper::aDominio).toList();
    }

    @Override
    public List<ServicioContratado> porUsuario(UUID usuarioId, int pagina, int tamano) {
        return jpa.findByUsuario(usuarioId, PageRequest.of(Math.max(0, pagina), Math.max(1, tamano))).stream()
                .map(ServicioPersistenceMapper::aDominio).toList();
    }

    @Override
    public long contarPorUsuario(UUID usuarioId) {
        return jpa.countByUsuario(usuarioId);
    }

    @Override
    public List<ServicioContratado> activosDeUsuario(UUID usuarioId) {
        return jpa.findActivosDeUsuario(usuarioId).stream().map(ServicioPersistenceMapper::aDominio).toList();
    }

    @Override
    public ServicioContratado guardar(ServicioContratado servicio) {
        return ServicioPersistenceMapper.aDominio(jpa.save(ServicioPersistenceMapper.aEntidad(servicio)));
    }
}
