package com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.persistence.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.Usuario;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.UsuarioRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.persistence.jpa.UsuarioJpaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.persistence.mapper.AuthPersistenceMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UsuarioPersistenceAdapter implements UsuarioRepository {
    private final UsuarioJpaRepository jpa;

    public UsuarioPersistenceAdapter(UsuarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Usuario> porId(UUID id) {
        return jpa.findById(id).map(AuthPersistenceMapper::aDominio);
    }

    @Override
    public Optional<Usuario> porEmail(String emailNormalizado) {
        return jpa.findByEmailIgnoreCase(emailNormalizado).map(AuthPersistenceMapper::aDominio);
    }

    @Override
    public Optional<Usuario> porEmailParaIniciarSesion(String emailNormalizado) {
        return jpa.findByEmailParaIniciarSesion(emailNormalizado).map(AuthPersistenceMapper::aDominio);
    }

    @Override
    public boolean existeEmail(String emailNormalizado) {
        return jpa.existsByEmailIgnoreCase(emailNormalizado);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        return AuthPersistenceMapper.aDominio(jpa.save(AuthPersistenceMapper.aEntidad(usuario)));
    }

    @Override
    public List<Usuario> buscarPorTexto(String texto) {
        List<Usuario> resultados = new ArrayList<>(
                jpa.buscarPorEmail(texto).stream().map(AuthPersistenceMapper::aDominio).toList());
        try {
            jpa.findById(UUID.fromString(texto.trim()))
                    .map(AuthPersistenceMapper::aDominio)
                    .filter(u -> resultados.stream().noneMatch(r -> r.id().equals(u.id())))
                    .ifPresent(resultados::add);
        } catch (IllegalArgumentException ignored) {
        }
        return resultados;
    }
}
