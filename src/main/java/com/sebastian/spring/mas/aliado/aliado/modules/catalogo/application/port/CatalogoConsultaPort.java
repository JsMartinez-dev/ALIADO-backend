package com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port;

import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.Categoria;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.Zona;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogoConsultaPort {
    Optional<Categoria> categoriaPorId(UUID id);
    Optional<Zona> zonaPorId(UUID id);
    Optional<Categoria> categoriaPorNombre(String nombre);
    Optional<Zona> zonaPorNombre(String nombre);
    List<Categoria> categoriasActivas();
    List<Zona> zonasActivas();
}
