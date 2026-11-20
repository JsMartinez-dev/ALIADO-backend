package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult.CompromisoResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.PerfilProfesionalNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ListarCompromisosAgendaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
import java.util.List;
import java.util.UUID;
public class ListarCompromisosAgendaService implements ListarCompromisosAgendaUseCase {
 private final PerfilProfesionalRepository perfiles;
 public ListarCompromisosAgendaService(PerfilProfesionalRepository perfiles) {this.perfiles=perfiles;}
 @Override public List<CompromisoResult> ejecutar(UUID prestadorId) {
 return perfiles.porPrestadorId(prestadorId).orElseThrow(PerfilProfesionalNoEncontradoException::new).compromisos().stream()
 .map(c -> new CompromisoResult(c.id(),c.fechaHoraInicio(),c.fechaHoraFin(),c.origen().name())).toList();
 }
}
