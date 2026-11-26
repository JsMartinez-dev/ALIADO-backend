package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.infrastructure.adapter;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.ConsultaOportunidadesPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.SolicitudConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.PerfilProfesionalConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoConsultaPort;
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
 private final SolicitudConsultaPort solicitudConsultaPort;
 private final PerfilProfesionalConsultaPort profesionalConsultaPort;
 private final CatalogoConsultaPort catalogoConsultaPort;
 private final PerfilVisibilidadPort perfilVisibilidadPort;
 public ConsultaOportunidadesAdapter(SolicitudConsultaPort solicitudes, PerfilProfesionalConsultaPort perfiles,
                                     CatalogoConsultaPort catalogo, PerfilVisibilidadPort visibilidad) {
  this.solicitudConsultaPort =solicitudes; this.profesionalConsultaPort =perfiles; this.catalogoConsultaPort =catalogo; this.perfilVisibilidadPort =visibilidad;
 }
 @Override public List<Solicitud> publicadas() {return solicitudConsultaPort.publicadas();}
 @Override public Optional<Solicitud> solicitudPorId(UUID id) {return solicitudConsultaPort.porId(id);}
 @Override public Optional<PerfilProfesional> perfilPorPrestadorId(UUID id) {return profesionalConsultaPort.porPrestadorId(id);}
 @Override public Optional<Categoria> categoriaPorId(UUID id) {return catalogoConsultaPort.categoriaPorId(id);}
 @Override public Optional<Categoria> categoriaPorNombre(String nombre) {return catalogoConsultaPort.categoriaPorNombre(nombre);}
 @Override public Optional<Zona> zonaPorId(UUID id) {return catalogoConsultaPort.zonaPorId(id);}
 @Override public Visibilidad preferenciaDe(UUID id) {return perfilVisibilidadPort.preferenciaDe(id);}
}
