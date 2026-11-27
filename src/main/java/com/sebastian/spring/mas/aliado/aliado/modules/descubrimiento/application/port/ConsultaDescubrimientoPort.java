package com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.port;

import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.Categoria;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.Zona;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ConfianzaConsultaPort.ConfianzaInfo;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ConfianzaConsultaPort.ResenaPublica;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Visibilidad;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Lecturas públicas requeridas por descubrimiento; no expone persistencia. */
public interface ConsultaDescubrimientoPort {
    Optional<PerfilProfesional> porPrestadorId(UUID prestadorId);
    List<PerfilProfesional> visiblesPorCategoriaYZona(UUID categoriaId, UUID zonaId);
    Optional<Categoria> categoriaPorId(UUID id);
    Optional<String> nombreVisibleDe(UUID usuarioId);
    Optional<Categoria> categoriaPorNombre(String nombre);
    Optional<Zona> zonaPorId(UUID id);
    Optional<Zona> zonaPorNombre(String nombre);
    List<Categoria> categoriasActivas();
    List<Zona> zonasActivas();
    Visibilidad preferenciaDe(UUID prestadorId);
    ConfianzaInfo confianzaDe(UUID prestadorId);
    List<ResenaPublica> resenasVisiblesDe(UUID prestadorId);
}

