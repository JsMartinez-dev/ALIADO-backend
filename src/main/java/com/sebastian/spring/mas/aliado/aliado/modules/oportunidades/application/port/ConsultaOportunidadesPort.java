package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Visibilidad;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.Categoria;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.Zona;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsultaOportunidadesPort {
 List<Solicitud> publicadas();
 Optional<Solicitud> solicitudPorId(UUID id);
 Optional<PerfilProfesional> perfilPorPrestadorId(UUID id);
 Optional<Categoria> categoriaPorId(UUID id);
 Optional<Categoria> categoriaPorNombre(String nombre);
 Optional<Zona> zonaPorId(UUID id);
 Visibilidad preferenciaDe(UUID prestadorId);
}
