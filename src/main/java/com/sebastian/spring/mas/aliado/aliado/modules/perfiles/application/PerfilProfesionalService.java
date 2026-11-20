package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.PerfilProfesionalConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.PerfilProfesionalModeracionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.EstadoExposicion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Perfil profesional (SPEC-05, UC024–UC033).
 * Un prestador, un perfil; crear exige al menos una categoría Y una zona (FR-024).
 * Conflicto disponibilidad/agenda (plan-05 T026): una disponibilidad que choca con un
 * compromiso ya registrado se rechaza con 409.
 */
@Service
public class PerfilProfesionalService
        implements PerfilProfesionalConsultaPort, PerfilProfesionalModeracionPort {
    private final PerfilProfesionalRepository perfiles;

    public PerfilProfesionalService(PerfilProfesionalRepository perfiles) {
        this.perfiles = perfiles;
    }

    /** Cambia el estado de exposición (SPEC-03): PUBLICO u OCULTO. */
    @Transactional
    public PerfilProfesional cambiarExposicion(UUID prestadorId, EstadoExposicion nuevo) {
        if (nuevo == null) {
            throw new BusinessException.BadRequest("EXPOSICION_INVALIDA",
                    "estadoExposicion debe ser PUBLICO u OCULTO.");
        }
        PerfilProfesional actual = propio(prestadorId);
        return perfiles.guardar(actual.conExposicion(nuevo));
    }

    @Override
    @Transactional
    public void ocultarPerfil(UUID prestadorId) {
        cambiarExposicion(prestadorId, EstadoExposicion.OCULTO);
    }

    private PerfilProfesional propio(UUID prestadorId) {
        return perfiles.porPrestadorId(prestadorId)
                .orElseThrow(() -> new BusinessException.NotFound("PERFIL_NO_ENCONTRADO",
                        "Primero debes crear tu perfil profesional."));
    }

    @Override
    public Optional<PerfilProfesional> porPrestadorId(UUID prestadorId) {
        return perfiles.porPrestadorId(prestadorId);
    }

    @Override
    public List<PerfilProfesional> visiblesPorCategoriaYZona(UUID categoriaId, UUID zonaId) {
        return perfiles.visiblesPorCategoriaYZona(categoriaId, zonaId);
    }
}
