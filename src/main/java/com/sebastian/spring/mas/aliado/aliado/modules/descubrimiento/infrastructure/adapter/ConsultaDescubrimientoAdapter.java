package com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.port.ConsultaDescubrimientoPort;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.Categoria;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.Zona;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ConfianzaConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ConfianzaConsultaPort.ConfianzaInfo;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ConfianzaConsultaPort.ResenaPublica;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.PerfilVisibilidadPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Visibilidad;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.PerfilProfesionalConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adapta contratos públicos existentes sin acceder a infraestructura de otros módulos. */
@Component
public class ConsultaDescubrimientoAdapter implements ConsultaDescubrimientoPort {
    private final PerfilProfesionalConsultaPort perfilProfesionalConsultaPort;
    private final CatalogoConsultaPort catalogoConsultaPort;
    private final ConfianzaConsultaPort confianzaConsultaPort;
    private final PerfilVisibilidadPort perfilVisibilidadPort;

    public ConsultaDescubrimientoAdapter(PerfilProfesionalConsultaPort perfiles, CatalogoConsultaPort catalogo,
                                         ConfianzaConsultaPort confianza, PerfilVisibilidadPort visibilidad) {
        this.perfilProfesionalConsultaPort = perfiles;
        this.catalogoConsultaPort = catalogo;
        this.confianzaConsultaPort = confianza;
        this.perfilVisibilidadPort = visibilidad;
    }
    @Override public Optional<PerfilProfesional> porPrestadorId(UUID prestadorId) {
        return perfilProfesionalConsultaPort.porPrestadorId(prestadorId);
    }
    @Override public List<PerfilProfesional> visiblesPorCategoriaYZona(UUID categoriaId, UUID zonaId) {
        return perfilProfesionalConsultaPort.visiblesPorCategoriaYZona(categoriaId, zonaId);
    }
    @Override public Optional<Categoria> categoriaPorId(UUID id) { return catalogoConsultaPort.categoriaPorId(id); }
    @Override public Optional<Categoria> categoriaPorNombre(String nombre) { return catalogoConsultaPort.categoriaPorNombre(nombre); }
    @Override public Optional<Zona> zonaPorId(UUID id) { return catalogoConsultaPort.zonaPorId(id); }
    @Override public Optional<Zona> zonaPorNombre(String nombre) { return catalogoConsultaPort.zonaPorNombre(nombre); }
    @Override public List<Categoria> categoriasActivas() { return catalogoConsultaPort.categoriasActivas(); }
    @Override public List<Zona> zonasActivas() { return catalogoConsultaPort.zonasActivas(); }
    @Override public Visibilidad preferenciaDe(UUID prestadorId) { return perfilVisibilidadPort.preferenciaDe(prestadorId); }
    @Override public ConfianzaInfo confianzaDe(UUID prestadorId) { return confianzaConsultaPort.confianzaDe(prestadorId); }
    @Override public List<ResenaPublica> resenasVisiblesDe(UUID prestadorId) {
        return confianzaConsultaPort.resenasVisiblesDe(prestadorId);
    }
}

