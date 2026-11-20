package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.PerfilPublicoResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.PerfilNoDisponibleException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConsultarPerfilPublicoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Perfil;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Ubicacion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.PerfilRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.UbicacionRepository;
import java.util.UUID;

public class ConsultarPerfilPublicoService implements ConsultarPerfilPublicoUseCase {
    private final PerfilRepository perfiles;
    private final UbicacionRepository ubicaciones;

    public ConsultarPerfilPublicoService(PerfilRepository perfiles, UbicacionRepository ubicaciones) {
        this.perfiles = perfiles;
        this.ubicaciones = ubicaciones;
    }

    @Override
    public PerfilPublicoResult ejecutar(UUID usuarioId) {
        Perfil perfil = perfiles.porUsuarioId(usuarioId).orElseThrow(PerfilNoDisponibleException::new);
        if (!perfil.preferenciaVisibilidad().permiteConsultaPublica()) {
            throw new PerfilNoDisponibleException();
        }
        Ubicacion ubicacion = ubicaciones.porUsuarioId(usuarioId).orElseThrow(PerfilNoDisponibleException::new);
        return new PerfilPublicoResult(usuarioId, ubicacion.zonaAproximada());
    }
}
