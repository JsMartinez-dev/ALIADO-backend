package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.PerfilBasePort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.PerfilBusquedaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.PerfilModeracionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Perfil;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Visibilidad;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.PerfilRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilService implements PerfilBasePort, PerfilModeracionPort, PerfilBusquedaPort {
    private final PerfilRepository perfilesRepository;

    public PerfilService(PerfilRepository perfilesRepository) {
        this.perfilesRepository = perfilesRepository;
    }

    @Override
    @Transactional
    public void crearPerfilBase(UUID usuarioId) {
        perfilesRepository.guardar(new Perfil(usuarioId, null, null, Visibilidad.SOLO_ZONA, 0));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Perfil> buscarPorNombre(String texto) {
        return perfilesRepository.buscarPorNombre(texto);
    }

    @Override
    @Transactional
    public void ocultarPerfil(UUID usuarioId) {
        Perfil actual = perfilesRepository.porUsuarioId(usuarioId)
                .orElseThrow(() -> new BusinessException.NotFound("PERFIL_NO_ENCONTRADO", "Perfil no encontrado."));
        perfilesRepository.guardar(new Perfil(usuarioId, actual.nombre(), actual.telefono(), Visibilidad.PRIVADO, actual.version()));
    }
}
