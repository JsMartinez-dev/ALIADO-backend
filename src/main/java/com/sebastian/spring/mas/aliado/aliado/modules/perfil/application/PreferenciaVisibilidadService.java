package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.NombreVisiblePerfilPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.PerfilVisibilidadPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Visibilidad;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.PerfilRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lectura de la preferencia de visibilidad (SPEC-02, FR-004).
 * Servicio dedicado y sin dependencias de otros servicios: rompería el ciclo
 * OportunidadService → PerfilService → ServicioService → PropuestaService →
 * OportunidadService si lo implementara PerfilService.
 */
@Service
public class PreferenciaVisibilidadService implements PerfilVisibilidadPort, NombreVisiblePerfilPort {
    private final PerfilRepository perfiles;

    public PreferenciaVisibilidadService(PerfilRepository perfiles) {
        this.perfiles = perfiles;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> nombreVisibleDe(UUID usuarioId) {
        return perfiles.porUsuarioId(usuarioId)
                .filter(perfil -> perfil.preferenciaVisibilidad() != Visibilidad.PRIVADO)
                .map(perfil -> perfil.nombre())
                .map(String::strip)
                .filter(nombre -> !nombre.isEmpty());
    }

    @Override
    @Transactional(readOnly = true)
    public Visibilidad preferenciaDe(UUID usuarioId) {
        return perfiles.porUsuarioId(usuarioId)
                .map(p -> p.preferenciaVisibilidad())
                .orElse(Visibilidad.SOLO_ZONA);
    }
}
