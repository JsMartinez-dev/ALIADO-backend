package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.PerfilResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.PerfilNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConsultarPerfilUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Perfil;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Ubicacion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.PerfilRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.UbicacionRepository;
import java.util.UUID;

/** UC011: consulta del perfil propio a través de los puertos del dominio. */
public class ConsultarPerfilService implements ConsultarPerfilUseCase {
    private final PerfilRepository perfiles;
    private final UbicacionRepository ubicaciones;

    public ConsultarPerfilService(PerfilRepository perfiles, UbicacionRepository ubicaciones) {
        this.perfiles = perfiles;
        this.ubicaciones = ubicaciones;
    }

    @Override
    public PerfilResult ejecutar(UUID usuarioId) {
        Perfil perfil = perfiles.porUsuarioId(usuarioId)
                .orElseThrow(PerfilNoEncontradoException::new);
        Ubicacion ubicacion = ubicaciones.porUsuarioId(usuarioId).orElse(null);
        return new PerfilResult(usuarioId, perfil.nombre(), perfil.telefono(),
                perfil.preferenciaVisibilidad().name(),
                ubicacion == null ? null : ubicacion.zonaAproximada(),
                ubicacion == null ? null : ubicacion.direccionExacta(),
                perfil.version());
    }
}
