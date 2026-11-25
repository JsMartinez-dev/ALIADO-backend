package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.PerfilResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.PerfilNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConsultarPerfilUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Perfil;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Ubicacion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.PerfilRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.UbicacionRepository;
import java.util.UUID;

public class ConsultarPerfilService implements ConsultarPerfilUseCase {
    private final PerfilRepository perfilesRepository;
    private final UbicacionRepository ubicacionesRepository;

    public ConsultarPerfilService(PerfilRepository perfilesRepository,
            UbicacionRepository ubicacionesRepository) {
        this.perfilesRepository = perfilesRepository;
        this.ubicacionesRepository = ubicacionesRepository;
    }

    @Override
    public PerfilResult ejecutar(UUID usuarioId) {
        Perfil perfil = perfilesRepository.porUsuarioId(usuarioId)
                .orElseThrow(PerfilNoEncontradoException::new);
        Ubicacion ubicacion = ubicacionesRepository.porUsuarioId(usuarioId).orElse(null);
        return new PerfilResult(usuarioId, perfil.nombre(), perfil.telefono(),
                perfil.preferenciaVisibilidad().name(),
                ubicacion == null ? null : ubicacion.zonaAproximada(),
                ubicacion == null ? null : ubicacion.direccionExacta(),
                perfil.version());
    }
}
