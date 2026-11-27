package com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.port.ConsultaDescubrimientoPort;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.Categoria;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.Zona;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ConfianzaConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ConfianzaConsultaPort.ConfianzaInfo;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ConfianzaConsultaPort.ResenaPublica;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.NombreVisiblePerfilPort;
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
    private final PerfilProfesionalConsultaPort perfiles;
    private final CatalogoConsultaPort catalogo;
    private final ConfianzaConsultaPort confianza;
    private final PerfilVisibilidadPort visibilidad;
    private final NombreVisiblePerfilPort nombres;

    public ConsultaDescubrimientoAdapter(PerfilProfesionalConsultaPort perfiles, CatalogoConsultaPort catalogo,
                                         ConfianzaConsultaPort confianza, PerfilVisibilidadPort visibilidad,
                                         NombreVisiblePerfilPort nombres) {
        this.perfiles = perfiles;
        this.catalogo = catalogo;
        this.confianza = confianza;
        this.visibilidad = visibilidad;
        this.nombres = nombres;
    }
    @Override public Optional<PerfilProfesional> porPrestadorId(UUID prestadorId) {
        return perfiles.porPrestadorId(prestadorId);
    }
    @Override public List<PerfilProfesional> visiblesPorCategoriaYZona(UUID categoriaId, UUID zonaId) {
        return perfiles.visiblesPorCategoriaYZona(categoriaId, zonaId);
    }
    @Override public Optional<String> nombreVisibleDe(UUID id) { return nombres.nombreVisibleDe(id); }
    @Override public Optional<Categoria> categoriaPorId(UUID id) { return catalogo.categoriaPorId(id); }
    @Override public Optional<Categoria> categoriaPorNombre(String nombre) { return catalogo.categoriaPorNombre(nombre); }
    @Override public Optional<Zona> zonaPorId(UUID id) { return catalogo.zonaPorId(id); }
    @Override public Optional<Zona> zonaPorNombre(String nombre) { return catalogo.zonaPorNombre(nombre); }
    @Override public List<Categoria> categoriasActivas() { return catalogo.categoriasActivas(); }
    @Override public List<Zona> zonasActivas() { return catalogo.zonasActivas(); }
    @Override public Visibilidad preferenciaDe(UUID prestadorId) { return visibilidad.preferenciaDe(prestadorId); }
    @Override public ConfianzaInfo confianzaDe(UUID prestadorId) { return confianza.confianzaDe(prestadorId); }
    @Override public List<ResenaPublica> resenasVisiblesDe(UUID prestadorId) {
        return confianza.resenasVisiblesDe(prestadorId);
    }
}

