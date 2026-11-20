package com.sebastian.spring.mas.aliado.aliado.modules.perfil.infrastructure.persistence.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Perfil;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.SolicitudEliminacion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.EstadoEliminacion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.PerfilRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.SolicitudEliminacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.infrastructure.persistence.jpa.PerfilJpaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.infrastructure.persistence.jpa.SolicitudEliminacionJpaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.infrastructure.persistence.mapper.PerfilPersistenceMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PerfilPersistenceAdapter implements PerfilRepository, SolicitudEliminacionRepository {
    private final PerfilJpaRepository jpaPerfil;
    private final SolicitudEliminacionJpaRepository eliminaciones;

    public PerfilPersistenceAdapter(PerfilJpaRepository jpaPerfil,
                                    SolicitudEliminacionJpaRepository eliminaciones) {
        this.jpaPerfil = jpaPerfil;
        this.eliminaciones = eliminaciones;
    }

    @Override
    public Optional<Perfil> porUsuarioId(UUID usuarioId) {
        return jpaPerfil.findById(usuarioId).map(PerfilPersistenceMapper::aDominio);
    }

    @Override
    public Perfil guardar(Perfil perfil) {
        return PerfilPersistenceMapper.aDominio(jpaPerfil.save(PerfilPersistenceMapper.aEntidad(perfil)));
    }

    @Override
    public List<Perfil> buscarPorNombre(String texto) {
        return jpaPerfil.findByNombreContainingIgnoreCase(texto).stream()
                .map(PerfilPersistenceMapper::aDominio).toList();
    }

    @Override
    public Optional<SolicitudEliminacion> pendienteDeUsuario(UUID usuarioId) {
        return eliminaciones.findByUsuarioIdAndEstado(usuarioId, "PENDIENTE")
                .map(PerfilPersistenceMapper::aDominio);
    }

    @Override
    public Optional<SolicitudEliminacion> ultimaDeUsuario(UUID usuarioId) {
        return eliminaciones.findFirstByUsuarioIdOrderByFechaSolicitudDesc(usuarioId)
                .map(PerfilPersistenceMapper::aDominio);
    }

    @Override
    public boolean transicionar(UUID id, EstadoEliminacion esperado, EstadoEliminacion nuevo,
                                OffsetDateTime fechaConfirmacion) {
        return eliminaciones.transicionar(id, esperado.name(), nuevo.name(), fechaConfirmacion) == 1;
    }

    @Override
    public void expirarVencidas(OffsetDateTime ahora) {
        eliminaciones.expirarVencidas(ahora);
    }

    @Override
    public List<SolicitudEliminacion> pendientesVencidas(OffsetDateTime ahora) {
        return eliminaciones.findPendientesVencidas(ahora).stream()
                .map(PerfilPersistenceMapper::aDominio).toList();
    }

    @Override
    public SolicitudEliminacion guardar(SolicitudEliminacion solicitud) {
        return PerfilPersistenceMapper.aDominio(
                eliminaciones.save(PerfilPersistenceMapper.aEntidad(solicitud)));
    }
}
