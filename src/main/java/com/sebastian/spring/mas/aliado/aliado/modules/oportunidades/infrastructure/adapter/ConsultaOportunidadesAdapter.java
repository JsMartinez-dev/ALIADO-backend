package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.infrastructure.adapter;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.ConsultaOportunidadesPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.SolicitudConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.PerfilProfesionalConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.NombreVisiblePerfilPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.PerfilVisibilidadPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.Categoria;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.Zona;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Visibilidad;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
@Component
public class ConsultaOportunidadesAdapter implements ConsultaOportunidadesPort {
 private final SolicitudConsultaPort solicitudes;
 private final PerfilProfesionalConsultaPort perfiles;
 private final CatalogoConsultaPort catalogo;
 private final PerfilVisibilidadPort visibilidad;
 private final NombreVisiblePerfilPort nombres;
 public ConsultaOportunidadesAdapter(SolicitudConsultaPort solicitudes, PerfilProfesionalConsultaPort perfiles,
                                     CatalogoConsultaPort catalogo, PerfilVisibilidadPort visibilidad,
                                         NombreVisiblePerfilPort nombres) {
  this.solicitudes=solicitudes; this.perfiles=perfiles; this.catalogo=catalogo; this.visibilidad=visibilidad;
  this.nombres = nombres;
 }
 @Override public List<Solicitud> publicadas() {return solicitudes.publicadas();}
 @Override public Optional<Solicitud> solicitudPorId(UUID id) {return solicitudes.porId(id);}
 @Override public Optional<PerfilProfesional> perfilPorPrestadorId(UUID id) {return perfiles.porPrestadorId(id);}
 @Override public Optional<String> nombreVisibleDe(UUID id) { return nombres.nombreVisibleDe(id); }
 @Override public Optional<Categoria> categoriaPorId(UUID id) {return catalogo.categoriaPorId(id);}
 @Override public Optional<Categoria> categoriaPorNombre(String nombre) {return catalogo.categoriaPorNombre(nombre);}
 @Override public Optional<Zona> zonaPorId(UUID id) {return catalogo.zonaPorId(id);}
 @Override public Visibilidad preferenciaDe(UUID id) {return visibilidad.preferenciaDe(id);}
}
