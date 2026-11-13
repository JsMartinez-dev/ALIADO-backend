package com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.Usuario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {
    Optional<Usuario> porId(UUID id);
    Optional<Usuario> porEmail(String emailNormalizado);
    Optional<Usuario> porEmailParaIniciarSesion(String emailNormalizado);
    boolean existeEmail(String emailNormalizado);
    Usuario guardar(Usuario usuario);
    List<Usuario> buscarPorTexto(String texto);
}
