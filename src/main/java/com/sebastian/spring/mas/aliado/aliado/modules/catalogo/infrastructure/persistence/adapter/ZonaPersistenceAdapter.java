package com.sebastian.spring.mas.aliado.aliado.modules.catalogo.infrastructure.persistence.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.EstadoCatalogo;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.infrastructure.persistence.mapper.CatalogoPersistenceMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.Zona;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.repository.ZonaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.infrastructure.persistence.jpa.ZonaJpaRepository;

@Component
public class ZonaPersistenceAdapter implements ZonaRepository {
    private final ZonaJpaRepository zonasJpa;

    public ZonaPersistenceAdapter(ZonaJpaRepository zonas) {
        this.zonasJpa = zonas;
    }

    @Override
    public Optional<Zona> porId(UUID id) {
        return zonasJpa.findById(id).map(CatalogoPersistenceMapper::aDominio);
    }

    @Override
    public Optional<Zona> porNombre(String nombre) {
        return zonasJpa.findByNombreIgnoreCase(nombre).map(CatalogoPersistenceMapper::aDominio);
    }

    @Override
    public List<Zona> todas() {
        return zonasJpa.findAll().stream().map(CatalogoPersistenceMapper::aDominio).toList();
    }

    @Override
    public List<Zona> activas() {
        return zonasJpa.findByEstado(EstadoCatalogo.ACTIVO.name()).stream()
                .map(CatalogoPersistenceMapper::aDominio).toList();
    }

    @Override
    public Zona guardar(Zona zona) {
        return CatalogoPersistenceMapper.aDominio(zonasJpa.save(CatalogoPersistenceMapper.aEntidad(zona)));
    }
}
