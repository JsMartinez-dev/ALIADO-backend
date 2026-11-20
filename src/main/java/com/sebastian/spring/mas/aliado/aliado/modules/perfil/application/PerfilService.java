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

/** Conserva los puertos de perfil consumidos por autenticación, búsqueda y moderación. */
@Service
public class PerfilService implements PerfilBasePort, PerfilModeracionPort, PerfilBusquedaPort {
    private final PerfilRepository perfiles;

    public PerfilService(PerfilRepository perfiles) {
        this.perfiles = perfiles;
    }

    @Override
    @Transactional
    public void crearPerfilBase(UUID usuarioId) {
        perfiles.guardar(new Perfil(usuarioId, null, null, Visibilidad.SOLO_ZONA, 0));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Perfil> buscarPorNombre(String texto) {
        return perfiles.buscarPorNombre(texto);
    }

    @Override
    @Transactional
    public void ocultarPerfil(UUID usuarioId) {
        Perfil actual = perfiles.porUsuarioId(usuarioId)
                .orElseThrow(() -> new BusinessException.NotFound("PERFIL_NO_ENCONTRADO", "Perfil no encontrado."));
        perfiles.guardar(new Perfil(usuarioId, actual.nombre(), actual.telefono(), Visibilidad.PRIVADO, actual.version()));
    }
}
