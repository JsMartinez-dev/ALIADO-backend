package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.FiltrarTableroCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.ConsultaOportunidadesPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.FiltrarTableroOportunidadesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import java.util.List;
import java.util.UUID;
public class FiltrarTableroOportunidadesService implements FiltrarTableroOportunidadesUseCase {
 private final ConsultaOportunidadesPort consultas;
 public FiltrarTableroOportunidadesService(ConsultaOportunidadesPort consultas) {this.consultas=consultas;}
 @Override public List<Solicitud> ejecutar(FiltrarTableroCommand command) {
  UUID categoria=null;
  if(command.categoria()!=null && !command.categoria().isBlank()) {
   try {categoria=UUID.fromString(command.categoria().trim());}
   catch(IllegalArgumentException ex) {categoria=consultas.categoriaPorNombre(command.categoria().trim()).map(c->c.id()).orElse(null);}
   if(categoria==null) {return List.of();}
  }
  UUID criterio=categoria;
  return command.compatibles().stream()
   .filter(s->criterio==null || s.categoriaId().equals(criterio))
   .filter(s->command.urgencia()==null || command.urgencia().isBlank()
     || (s.urgencia()!=null && s.urgencia().name().equalsIgnoreCase(command.urgencia())))
   .toList();
 }
}
