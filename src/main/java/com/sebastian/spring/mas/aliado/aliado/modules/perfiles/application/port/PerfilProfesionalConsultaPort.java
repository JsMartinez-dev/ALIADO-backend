package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port;

import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerfilProfesionalConsultaPort {
    Optional<PerfilProfesional> porPrestadorId(UUID prestadorId);
    List<PerfilProfesional> visiblesPorCategoriaYZona(UUID categoriaId, UUID zonaId);
}
